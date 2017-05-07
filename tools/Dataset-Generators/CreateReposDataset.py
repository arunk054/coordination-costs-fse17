from argparse import ArgumentParser
import os
import json,csv
import re

class Repo:

	def __init__(self):
		self.stars = 0
		self.watchers = 0
		self.forks = 0
		self.isFork = False
		self.size = 0
		self.startDate = ''
		self.pushed_at = ''
		self.contributors = set()	
	
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

def getRepoDetails(fileName, contribFileName, foundContribs):
	fileHandle  = open(fileName,'r',encoding='utf-8')
	repoJson = json.loads(fileHandle.read())
	repoJson = repoJson[0]
	fileHandle.close()
	curRepo = Repo()
	curRepo.stars = repoJson['stargazers_count']
	curRepo.watchers = repoJson['subscribers_count']
	curRepo.forks = repoJson['forks_count']
	curRepo.size = int(repoJson['size'])
	curRepo.startDate = repoJson['created_at']
	curRepo.pushed_at = repoJson['pushed_at']
	curRepo.isFork = repoJson['fork']
	curRepo.contributors.add(repoJson['owner']['login'])
	if not foundContribs:
		return curRepo
		
	fileHandle  = open(contribFileName,'r',encoding='utf-8')
	arr = json.loads(fileHandle.read())
	fileHandle.close()
	
	for contrib in arr:
		curRepo.contributors.add(contrib['login'])
		
	return curRepo
	
def getReposDict (jsonDir):
	reposDict = dict()
	for entry in os.scandir(jsonDir):
		#print(entry.path)
		if entry.name.endswith('__E__contributors'): # we will only look for issues and then get the corresponding comments
			continue

		contribsFileName = entry.name+"contributors"
		contribsFile = os.path.join(jsonDir,contribsFileName)
		foundContribs = True
		if not os.path.isfile(contribsFile):
			print("MYERROR: contribs file not found",contribsFileName)
			foundContribs = False
			
		repoFile = entry.path
		repoName = getRepoName(entry.name)		
		curRepo = getRepoDetails(repoFile, contribsFile, foundContribs)
		reposDict[repoName] = curRepo
	return reposDict

def writeToCSV(repoIssuesDict, outCSVFileName):
	with open(outCSVFileName, 'w') as repoCSV:
		#write header
		csv.writer(repoCSV).writerow(['Repo','StartDate','PushedAt','Stars','Watchers','Forks','isFork', 'size', 'Contributors'])
		for k,repo in repoIssuesDict.items():
			row = [k]
			row.append(repo.startDate)
			row.append(repo.pushed_at)
			row.append(repo.stars)
			row.append(repo.watchers)
			row.append(repo.forks)
			row.append(repo.isFork)
			row.append(repo.size)
			row.append(repo.contributors)		
			csv.writer(repoCSV).writerow(row)
		
def main():
	args = getArgs()

	myDict =	getReposDict(args.inputJsonDir)
	writeToCSV(myDict, args.outputFileName)
	
if __name__ == "__main__":
	main()