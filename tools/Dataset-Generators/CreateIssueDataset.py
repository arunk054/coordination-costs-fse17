from argparse import ArgumentParser
import os
import json,csv
import re


class Issue:

	def __init__(self,issueId, is_open, is_pullrequest):
		self.issueId = issueId
		self.is_open = is_open
		self.is_pullrequest = is_pullrequest
		self.uniqueUsers = set()
		self.closedAt = None
		self.isEnhancement = False
		self.createdAt = None
		self.crossRefs  = None
		self.reporterLogin  = None
		self.totalComments = 0
		self.intRefs = []
		self.extRefs = []

	def updateIntExtCrossRefs(self, owner, repo):
		key = owner+ "__"+repo+"__"
		key = key.lower()
		for ref in self.crossRefs:
			if ref.startswith(key):
				self.intRefs.append(ref)
			else:
				self.extRefs.append(ref)
		
def getArgs():
	parser = ArgumentParser( description="")
	parser.add_argument('inputJsonDir',help='directory of github issue json files')
	parser.add_argument('outputFileName',help='')
	args = parser.parse_args()	
	return args

def getRepoName(fileName):
	repoSep = '__R__'
	index1 = fileName.index(repoSep)
	index2 = fileName.index('__E__')
	return fileName[:index1] + "/" +fileName[index1+len(repoSep):index2]
	
def getCountMatches(id,ownerRepoArr,pattern,txt, isExt):
	refsId = set()
	if not txt:
		return refsId	
	index = 0
	length = len(txt)
	while True:
		m = pattern.search(txt,index)
		if not m:
			return refsId
		if not isExt:
			start = m.start()
			end = m.end()
			#print("***","__".join([ownerRepoArr[0],ownerRepoArr[1],txt[start+1:end]]))
			refsId.add("__".join([ownerRepoArr[0].lower(),ownerRepoArr[1].lower(),txt[start+1:end]]))
		else:
			pTxt = m.groups()[0]

			index1 = pTxt.index('github.com/')+len('github.com/')
			index2 = pTxt.index('/',index1)
			owner = pTxt[index1:index2].lower()
			
			index1 = index2+1
			index2 = pTxt.index('/',index1)
			repo = pTxt[index1:index2].lower()
			
			index1 = index2+1
			index2 = pTxt.index('/', index1)+1
			refId = pTxt[index2:]
			#if id == 1211 and ownerRepoArr[0] == 'benoitc' and ownerRepoArr[1] == 'gunicorn':
			#	print("***",owner,repo,refId)
			refsId.add('__'.join([owner,repo,refId]))
			#refsId.add("__".join([m.groups()[1].lower(),m.groups()[3][0:m.groups()[3].index('/')].lower(),m.groups()[4]]))
		index = m.end()
		
#Little bit messy just needs cleanup
def getCrossRefs(id,repoName,txt):
	#exp = '#[\\d]+'
	#pattern = re.compile(exp,re.IGNORECASE)
	ownerRepoArr = repoName.split('/')
	crossRefs = set()
	#crossRefs = getCountMatches(id,ownerRepoArr,pattern,txt,False)
	exp = "(github.com/.[^/]*?/.[^/]*?/issues/[\\d]+)"
	pattern = re.compile(exp,re.IGNORECASE)
	crossRefs.update(getCountMatches(id,ownerRepoArr,pattern,txt,True))
	exp = "(github.com/.[^/]*?/.[^/]*?/pull/[\\d]+)"
	pattern = re.compile(exp,re.IGNORECASE)
	crossRefs.update(getCountMatches(id,ownerRepoArr,pattern,txt,True))
	return crossRefs
	
def getCommentsDict(repoName,fileName):
	commentsDict = dict()
	#file must exist
	fileHandle= open(fileName,'r',encoding='utf-8')
	arr = json.loads(fileHandle.read())
	fileHandle.close()
	for comment in arr:
		try:
			issue_url = comment['issue_url']
			id = int(issue_url[issue_url.rindex('/')+1:])
			
			if id not in commentsDict:
				commentsDict[id]=[]
			crossRefs = getCrossRefs(id,repoName,comment['body'])
			commentsDict[id].append((comment['user']['login'], crossRefs))
		except ValueError as e:
			print("MYERROR: reading file ", fileName, e)
	return commentsDict

ENHANCEMENT_LABELS = ['feature','enhancement']

def getIssues(repoName,issuesFileName):
	issueHandle  = open(issuesFileName,'r',encoding='utf-8')
	arr = json.loads(issueHandle.read())
	issueHandle.close()
	
	issues = []
	for issue in arr:
		is_open = True
#		if 'state' in issue and issue['state'].lower() == 'open':
#			is_open = True
#		if "closed_at" in issue and issue["closed_at"]:
#			is_open=False
#Tweaked the above to check for state
		if 'state' in issue and issue['state'].lower() != 'open' and "closed_at" in issue and issue["closed_at"]:
			is_open = False
		is_pullrequest = False
		if 'pull_request' in issue:
			is_pullrequest = True
		issueId = issue['number']
		curIssue = Issue(issueId, is_open, is_pullrequest)
		curIssue.createdAt = issue['created_at']
		if not is_open:
			curIssue.closedAt=  issue['closed_at']
		if 'labels' in issue:
			for label in issue['labels']:
				for filter in ENHANCEMENT_LABELS:
					if 'name' in label and filter in label['name'].lower():
						curIssue.isEnhancement = True
						break
		crossRefs = getCrossRefs(curIssue.issueId,repoName,issue['body'])
		curIssue.crossRefs = crossRefs
		curIssue.reporterLogin = issue['user']['login']
		curIssue.uniqueUsers.add(curIssue.reporterLogin)
		issues.append(curIssue)
	return issues
	
def getRepoIssuesDict (jsonDir):
	repoIssuesDict = dict()
	for entry in os.scandir(jsonDir):
		#print(entry.path)
		if entry.name.endswith('__comments'): # we will only look for issues and then get the corresponding comments
			continue

		commentsFileName = entry.name+"__comments"
		commentsFile = os.path.join(jsonDir,commentsFileName)
		if not os.path.isfile(commentsFile):
			print("MYERROR: comments file not found",commentsFileName)
			continue
			
		issueFile = entry.path
		repoName = getRepoName(entry.name)		
		ownerRepoArr = repoName.split('/')
		issuesList = getIssues(repoName,issueFile)
		commentsDict = getCommentsDict(repoName,commentsFile)
		for issue in issuesList:
			try:
				commentsList = commentsDict[issue.issueId]
				for comment in commentsList:
					issue.uniqueUsers.add(comment[0].strip())
					issue.crossRefs.update(comment[1])
					issue.totalComments+=1
			except KeyError as e:
				pass
			#hack to remove refs to same issue
			issueKey = "__".join([ownerRepoArr[0],ownerRepoArr[1],str(issue.issueId)])
			issue.crossRefs.discard(issueKey)
			issue.updateIntExtCrossRefs(ownerRepoArr[0],ownerRepoArr[1])

		repoIssuesDict[repoName] = issuesList
	return repoIssuesDict

def writeToCSV(repoIssuesDict, outCSVFileName):

	with open(outCSVFileName, 'w') as repoCSV:
		#write header
		csv.writer(repoCSV).writerow(['Repo','IssueId','isOpen','isPullRequest','isEnhancement', 'IntCrossRefs' ,'ExtCrossRefs','CreatedAt','ClosedAt','Comments','UniqueUsers','ReporterLogin'])
		for k,v in repoIssuesDict.items():
			for issue in v:
				row = [k]
				row.append(issue.issueId)
				row.append(issue.is_open)
				row.append(issue.is_pullrequest)
				row.append(issue.isEnhancement)
				if len(issue.intRefs)+len(issue.extRefs) > 0:
					print("Issue with cross refs:",len(issue.intRefs),len(issue.extRefs))
				row.append(len(issue.intRefs))
				row.append(len(issue.extRefs))
				row.append(issue.createdAt)
				row.append(issue.closedAt)
				row.append(issue.totalComments)
				row.append(len(issue.uniqueUsers))
				row.append(issue.reporterLogin)
				csv.writer(repoCSV).writerow(row)
		
def main():
	args = getArgs()

	myDict =	getRepoIssuesDict(args.inputJsonDir)
	writeToCSV(myDict, args.outputFileName)
	
if __name__ == "__main__":
	main()