from argparse import ArgumentParser
import os
import json,csv
import re

class Issue:
	def __init__(self,issueId, is_open, is_pullrequest):
		self.issueId = issueId
		self.is_open = is_open
		self.is_pullrequest = is_pullrequest
		self.textList = ["",""]#text, body -> rest will be comments
	def addTitle(self, title):
		self.textList[0] = title
	def addBody(self, body):
		self.textList[1] = body
		
def getArgs():
	parser = ArgumentParser( description="")
	parser.add_argument('inputJsonDir',help='directory of github json files')
	parser.add_argument('dependenciesFile',help='consolidated final set of dependencies per repo')
	parser.add_argument('outputFileName',help='')
	args = parser.parse_args()	
	return args

def getRepoName(fileName):
	repoSep = '__R__'
	index1 = fileName.index(repoSep)
	index2 = fileName.index('__E__')
	return fileName[:index1] + "/" +fileName[index1+len(repoSep):index2]
	

def getCommentsDict(fileName):
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
			commentsDict[id].append(comment['body'])
		except ValueError as e:
			print("MYERROR: reading file ", fileName, e)
	return commentsDict
	
def getIssues(issuesFileName):
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
		if 'state' in issue and issue['state'].lower() != 'open' and "closed_at" in issue and issue["closed_at"]:
			is_open = False
			
		is_pullrequest = False
		if 'pull_request' in issue:
			is_pullrequest = True
		issueId = issue['number']
		curIssue = Issue(issueId, is_open, is_pullrequest)
		curIssue.addTitle(issue['title'])
		curIssue.addBody(issue['body'])
		issues.append(curIssue)			
	return issues

def getDependenciesMap(fileName):
	depsDict = dict()
	with open(fileName, 'r',encoding='utf-8') as depsCSV:
		curReader = csv.reader(depsCSV)
		next(curReader)
		for row in curReader:
			depsDict[row[1]] = row[2].split(';')
	return depsDict

def removeCodeBlocks(txt):
	if not txt:
		return ''
	TRIPLE_BACK_TICKS = '```'
	skip = len(TRIPLE_BACK_TICKS)
	startBlock = -1
	retText = ""
	curIndex = 0
	index = 0 - skip
	while True:
		try:
			index = txt.index(TRIPLE_BACK_TICKS, index+skip)
			if startBlock == -1:
				startBlock = index	
			else:
				retText += txt[curIndex:startBlock]
				curIndex = index + skip
				startBlock = -1
			#print("curIndex",curIndex)
			if curIndex == len(txt):
				break
		except ValueError:
			retText += txt[curIndex:len(txt)]
			break

	return retText
TICK_PREFIX = "__tick__"

def getCountMatches(pattern,txt):
	count = 0
	tickCount = 0
	index = 0
	length = len(txt)
	while True:
		m = pattern.search(txt,index)
		#print("Index",index)
		if not m:
			return (count,tickCount)
		start = m.start()
		end = m.end()
		if start > 0 and txt[start - 1] == '`' and end < length and txt[end] == '`':
			tickCount +=1
		else:
			count+=1
		index = m.end()


def classifyExternal(issuesList, commentsDict, dependencies):
	#exp = "(?<![\\w])"+patternName+"(?![a-zA-Z])" ; re.compile(exp,re.IGNORECASE)
	returnList=[]
	for issue in issuesList:
		print ("Processing",issue.issueId)
		try:
			issue.textList.extend(commentsDict[issue.issueId])
		except KeyError:
			pass
			#print("No Comments for:",issue.issueId)

		extMap = dict()
		for dep in dependencies:
			dep = dep.strip()
			if not dep:
				continue
			#do not do alternate names or acrnonyms for dependencies e.g. ggplot or ggp is acn name of ggplot2 
			exp = "(?<![\\w])"+dep+"(?![a-zA-Z])"
			pattern = re.compile(exp,re.IGNORECASE)
			megaTxt = ''
			for txt in issue.textList:
				#remove out code snippets				
				megaTxt = megaTxt+' ' + removeCodeBlocks(txt)
				
			count,tickCount = getCountMatches(pattern, megaTxt)
			if count == 0 and tickCount == 0:
				continue	
			counts = [count,tickCount]
			keys = [dep,TICK_PREFIX+dep]
			for i in range(0,2):
				if counts[i] == 0:
					continue	
				if not keys[i] in extMap:
					extMap[keys[i]] = 0
				extMap[keys[i]] += counts[i]			

		is_external = True
		if len(extMap) == 0:
			is_external = False
		returnList.append((issue.issueId, issue.is_open, issue.is_pullrequest, is_external, extMap))
	return returnList
	
def getRepoIssuesDict(jsonDir, depsFile):
	repoIssuesDict = dict() #key = reponame, list of Issue tuples
	depsDict = getDependenciesMap(depsFile)
	
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
		
		issuesList = getIssues(issueFile)
		commentsDict = getCommentsDict(commentsFile)
		#exp = "(?<![\\w])"+patternName+"(?![a-zA-Z])" ; re.compile(exp,re.IGNORECASE)
		try:
			dependencies = depsDict[repoName]
		except KeyError:
			print("MYERROR: Dependencies not found for",repoName)
			continue
		print("Repo:",repoName)
		issueTuples = classifyExternal(issuesList, commentsDict, dependencies) 
		#we can later filter out deps that are builtin and deps that are dictionary words and see how it shows.

		repoIssuesDict[repoName] = issueTuples #issueid, isExternal, isOPEN, isPR, extDict
	return repoIssuesDict

def writeIssuesToCSV(csvFileName, repoIssuesDict):
	with open(csvFileName, 'w') as repoCSV:
		#write header
		csv.writer(repoCSV).writerow(['Repo','IssueId','isOpen','isPullRequest','isExternal','DependencyNameDict'])	
		for k,v in repoIssuesDict.items():
			for issue in v:
				row = [k]
				row.extend(issue)
				csv.writer(repoCSV).writerow(row)

def main():
	args = getArgs()
	myDict =	getRepoIssuesDict(args.inputJsonDir, args.dependenciesFile)
	print("Total Repos: ",len(myDict))
	writeIssuesToCSV(args.outputFileName, myDict)
#	for k,v in myDict.items():
#		print(k,v)
	
	
if __name__ == "__main__":
	main()

