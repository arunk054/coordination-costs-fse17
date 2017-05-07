from argparse import ArgumentParser
import os
import json,csv
from datetime import datetime
import copy

TICK_PREFIX = '__tick__'
TICK_PREFIX_LEN = len(TICK_PREFIX)
EPOCH_START_UTC_TIME = datetime.utcfromtimestamp(0)
CURRENT_EPOCH_TIME = int((datetime.now() - EPOCH_START_UTC_TIME).total_seconds())

#def isAllBelongsToBuiltins(depsDict,builtins):
#	if len(depsDict) == 0:
#		return False
#	for k in depsDict:
#		if k.startswith(TICK_PREFIX):
#			k = k[TICK_PREFIX_LEN:]
#		if k.lower() not in builtins:
#			return False
#	return True

depsDictWords =set()
def addDepsDictWords(depsDict,dictWords):
	for k in depsDict:
		dep = k.lower()
		if dep.startswith(TICK_PREFIX):
			dep = dep[TICK_PREFIX_LEN]
		if dep in dictWords:
			depsDictWords.add(dep)

def filterWords(depsDict,dictWords, reverseLogic, isEng):
	#copy of depsDict - deepcopy probably overkill
	depsDict1 = dict()
	for k in depsDict:
		dep = k.lower()
		#Dont filter words that enclosed within TICK because they are valid deps
		if (not isEng or reverseLogic) and dep.startswith(TICK_PREFIX):
			dep = dep[TICK_PREFIX_LEN:]
		if (not reverseLogic and dep not in dictWords) or (reverseLogic and dep in dictWords):
			depsDict1[k] = depsDict[k]
	return depsDict1
	
def writeToCSV(reposFullDict, repoPkgMap, builtins, dictWords, outFileName):
	#Header looks like
	header = ['repo', 'issue_id', 'is_open', 'is_pullrequest', 'is_feature', 'is_external','deps_count',
	 'is_external_wo_builtin','is_external_with_builtin', 'is_external_wo_eng_words','is_external_with_eng_words',
	  'is_external_wo_eng_words_wo_builtin','is_external_wo_eng_words_with_builtin','int_refs',
	  'ext_refs','crossrefs', 'time_in_secs', 'comments', 'users', 'is_reporter_developer', 
	  'repo_age_secs',  'repo_stars', 'repo_watchers','repo_forks', 'repo_is_fork', 'repo_size_kb',
	   'repo_contributors', 'reporter','pkgName']
	
	with open(outFileName, 'w') as outCSV:
		#write header
		csv.writer(outCSV).writerow(header)

		for repo,repoDict in reposFullDict.items():
			for issueId, issueDict in repoDict.items():
				if type(issueId) is not int:
					continue
				row = [repo,issueId]
				#print(issueDict)
				row.append(issueDict['is_open'])
				row.append(issueDict['is_pullrequest'])
				row.append(issueDict['is_feature'])
				depsDict = issueDict['deps_dict']
				if type(depsDict) is not dict:
					print("MYERROR NOTDICT", type(depsDict))

				row.append(False if len(depsDict) == 0 else True)
				row.append(len(depsDict))
				
				addDepsDictWords(depsDict, dictWords)
				depsDict1 = filterWords(depsDict, builtins, False, False)
				row.append(False if len(depsDict1) == 0 else True)
				depsDict1 = filterWords(depsDict, builtins, True, False)
				row.append(False if len(depsDict1) == 0 else True)

				depsDict1 = filterWords(depsDict, dictWords, False, True)
				row.append(False if len(depsDict1) == 0 else True)
				depsDict1 = filterWords(depsDict, dictWords, True, True)
				row.append(False if len(depsDict1) == 0 else True)				


				depsDict1 = filterWords(depsDict, dictWords, False, True)
				depsDict2 = filterWords(depsDict1, builtins, False, False)
				row.append(False if len(depsDict2) == 0 else True)
				depsDict2 = filterWords(depsDict1, builtins, True, False)
				row.append(False if len(depsDict2) == 0 else True)				

				
				row.append(issueDict['int_refs'])
				row.append(issueDict['ext_refs'])
				row.append(issueDict['int_refs']+issueDict['ext_refs'])
				
				row.append(issueDict['time_secs'])
				row.append(issueDict['comments'])
				row.append(issueDict['users'])
				
				row.append(issueDict['is_reporter_contrib'])
				
				row.append(CURRENT_EPOCH_TIME - repoDict['creation_time_epoch'])
				
				row.append(repoDict['stars'])
				row.append(repoDict['watchers'])
				row.append(repoDict['forks'])
				
				row.append(repoDict['is_fork'])
				row.append(repoDict['size'])
				row.append(repoDict['contributors'])
				row.append(issueDict['reporter'])
				
				pkgName = "UNKNOWN_PKG"
				try:
					pkgName = repoPkgMap[repo]
				except KeyError:
					pass
				row.append(pkgName)
				
				csv.writer(outCSV).writerow(row)
			
				
def getDict(fullDict,key):
	try:
		return fullDict[key]
	except KeyError:
		fullDict[key] = dict()
		return fullDict[key]


def getIssuesDict(reposFullDict, repo, issueId):
	curDict = getDict(reposFullDict,repo)
	return getDict(curDict,issueId)

def getEpochTimeSeconds(str):
	#Let others catch exception - ValueError
	ut_str = datetime.strptime(str,"%Y-%m-%dT%H:%M:%SZ")
	return int((ut_str - EPOCH_START_UTC_TIME).total_seconds())
	
def getTimeDiff(created, closed):
	if not closed or not created:
		return 0 #not available
	#get utc time:
	try:
		return getEpochTimeSeconds(closed) - getEpochTimeSeconds(created)
	except ValueError as e:
		print("MYERROR: parsing creation or closing time",e)
		return -1 #some error in formatting
	
def buildRepos(fileName,reposFullDict):
	with open(fileName, 'r',encoding='utf-8') as myCSV:
		curReader = csv.reader(myCSV)
		next(curReader)#header
		for row in curReader:
			repo = row[0]
			repoDict = getDict(reposFullDict,repo)
			repoDict['creation_time_epoch'] = getEpochTimeSeconds(row[1])
			
			try:
				repoDict['pushed_time_epoch'] = getEpochTimeSeconds(row[2])
			except Exception as e:
				repoDict['pushed_time_epoch'] = 0
			repoDict['stars'] = int(row[3])
			repoDict['watchers'] = int(row[4])
			repoDict['forks'] = int(row[5])
			repoDict['is_fork'] = eval(row[6])
			repoDict['size'] = int(row[7])
			try:
				repoDict['contributor_set'] = eval(row[8])
			except:
				print("MYERROR: error evaluating contributors",row[8])
				repoDict['contributor_set'] = set()
			repoDict['contributors'] = len(repoDict['contributor_set'])

def buildIssues(fileName,reposFullDict):
	total = 0
	with open(fileName, 'r',encoding='utf-8') as myCSV:
		curReader = csv.reader(myCSV)
		next(curReader)#header
		for row in curReader:
			repo = row[0]
			repoDict = getDict(reposFullDict, repo)
			issueId = int(row[1])
			issueDict = getDict(repoDict,issueId)
			
			#skip this if we could not find issueDict in classified csv
			if len(issueDict) == 0:
				#Sort of expected
				#print("MYERROR: Issue not found in issues csv", repo, issueId)
				del repoDict[issueId]
				continue
			issueDict['is_open'] = eval(row[2])
			issueDict['is_pullrequest'] = eval(row[3])
			issueDict['is_feature']=eval(row[4])
			issueDict['int_refs']=int(row[5])
			issueDict['ext_refs']=int(row[6])
			issueDict['time_secs'] = getTimeDiff(row[7],row[8])
			issueDict['comments'] = int(row[9])
			issueDict['users'] = int(row[10])
			issueDict['reporter'] = row[11]
			if row[11] in repoDict['contributor_set']:
				issueDict['is_reporter_contrib'] = True
			else:
				issueDict['is_reporter_contrib'] = False
			total+=1
	return total
			
def buildClassifiedIssues(fileName,reposFullDict):
	total = 0
	with open(fileName, 'r',encoding='utf-8') as myCSV:
		curReader = csv.reader(myCSV)
		next(curReader)#header
		for row in curReader:
			repo = row[0]
			repoDict = getDict(reposFullDict, repo)
			if len(repoDict) == 0:
				print("MYERROR: Repo not found in classified csv", repo)
				del reposFullDict[repo]
				continue
			issueId = int(row[1])
			issueDict = getDict(repoDict,issueId)
			#issueDict['is_open'] = eval(row[2])
			#issueDict['is_pullrequest'] = eval(row[3])
			issueDict['deps_dict'] = eval(row[5])
			total+=1
	return total
	
def getPkGRepoMap(fileName):
	repoPkgMap = dict()
	with open(fileName, 'r',encoding='utf-8') as myCSV:
		curReader = csv.reader(myCSV)
		next(curReader)#header
		for row in curReader:
			pkg = row[0]
			repo = row[1]
			if not pkg or not repo:
				continue
			repoPkgMap[repo] = pkg
	return repoPkgMap

def getDictWords(fileName):
	dictWords = set()
	with open(fileName, 'r',encoding='utf-8') as myCSV:
		curReader = csv.reader(myCSV)
		#next(curReader)# No header in this case
		for row in curReader:
			try:
				dictWords.add(row[0].lower())
			except IndexError: #empty rows
				pass
	return dictWords

def getBuiltins(fileName):
	builtins = set()
	with open(fileName, 'r',encoding='utf-8') as myCSV:
		curReader = csv.reader(myCSV)
		next(curReader)#header
		for row in curReader:
			builtins.add(row[0].lower())
	return builtins
	
def getArgs():
	parser = ArgumentParser( description="")
	parser.add_argument('reposDatasetCSV',help='')	
	parser.add_argument('issuesDatasetCSV',help='')
	parser.add_argument('classifiedIssuesCSV',help='')
	parser.add_argument('pkgRepoMapCSV',help='')		
	parser.add_argument('builtins',help='')	
	parser.add_argument('dictionary',help='')	
	parser.add_argument('outFileName',help='')	
	
	args = parser.parse_args()	
	return args

def missingRepos(reposFullDict,repoPkgMap):
	retList  =  []
	for k in reposFullDict:
		if k not in repoPkgMap:
			retList.append(k)
	return retList
	
def main():
	args = getArgs()
	reposFullDict = dict()
	buildRepos(args.reposDatasetCSV,reposFullDict)
	print("Built Repos:", len(reposFullDict))
	tot = buildClassifiedIssues(args.classifiedIssuesCSV,reposFullDict)
	print("Built Classified",tot)
	tot = buildIssues(args.issuesDatasetCSV,reposFullDict)
	print("Built Issues", tot)
	repoPkgMap = getPkGRepoMap(args.pkgRepoMapCSV)
	misList = missingRepos(reposFullDict,repoPkgMap)
	print("Missing repos in pkg repo map",len(misList))
	
	print("Built repo pkg map", len(repoPkgMap))
	builtIns = getBuiltins(args.builtins)
	print("Total builtins",len(builtIns))
	
	dictWords = getDictWords(args.dictionary)
	print("Total dictionary words",len(dictWords))
	
	writeToCSV(reposFullDict, repoPkgMap, builtIns, dictWords, args.outFileName)
	print("Total dependencies as dictionary words:",len(depsDictWords))
	
if __name__ == "__main__":
	main()