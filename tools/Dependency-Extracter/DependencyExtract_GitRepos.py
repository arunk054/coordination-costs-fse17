import ast
import logging
import os
import zipfile,tarfile
import shutil
import argparse
import csv
import json, codecs
import sys
import tempfile as tf
import pygit2


class MyEncoder(json.JSONEncoder):
    def default(self, o):
        return o.__dict__   
        
class DependencyObject(MyEncoder):
	def __init__(self, fileName, deps):
		self.fileName = fileName
		self.deps = deps
	def __str__(self):
		return json.dumps(self.__dict__)

#	def __repr__(self):
#		return json.dumps(self.__dict__)

def getImportedModulesFromFile(file):
	returnSet=set()
	global files

	try:
		tree = ast.parse(codecs.open(file, 'r', encoding = 'utf-8',errors='ignore').read())

		for node in ast.walk(tree):
			if isinstance(node, ast.Import):
				for moduleName in node.names:
					if isinstance(moduleName, ast.alias):
						moduleName=moduleName.name
					if moduleName:
						returnSet.add(moduleName)
			elif isinstance(node,ast.ImportFrom):
				if node.module and (not node.level or node.level==0): #level==0 indicates absolute imports
					returnSet.add(node.module)
	except:
		print("Error ast parse", file, "Error:",sys.exc_info()[0])
		#logging.exception('')
		
	return returnSet

def extractArchive(fileFullPath,tempDir):
	""" returns the full path of extracted file """
	extns = [".zip",".tar.gz",".tgz",".tar.bz2",".tbz"]
	openMode = ['r:gz','r:bz2']


	if fileFullPath.endswith(extns[0]):
		with zipfile.ZipFile(fileFullPath) as arch:
			arch.extractall(path=tempDir)
			return os.path.join(tempDir, os.path.basename(fileFullPath)[:-len(extns[0])])
	for i in range(1,len(extns)):
		if fileFullPath.endswith(extns[i]):
			with tarfile.open(fileFullPath,openMode[(i-1)//2]) as arch:
				arch.extractall(path=tempDir)
			return os.path.join(tempDir, os.path.basename(fileFullPath)[:-len(extns[i])])

def getArgs():
	parser = argparse.ArgumentParser( description="Get Dependencies")
	parser.add_argument('inputFile',help='mapping of git repo to pkg')
	parser.add_argument('--repoToContinue', help='Name of the repo to continue from, leave empty to start from beginning')
	args = parser.parse_args()	
	return args
	
def getAllDependencies(extractedDir):
	#depSet = set()
	depsList = []#list of DependencyObject
	for root,dirs,files in os.walk(extractedDir):
		for f in files:
			if f.endswith('.py'):
				fileFullPath = os.path.join(root,f)
				depsList.append(DependencyObject(fileFullPath,list(getImportedModulesFromFile(fileFullPath))))
				#depSet |= getImportedModulesFromFile(os.path.join(root,f))
	return depsList
	
def writeToFile(pkgVersionList,dependencySet,outFile):
	print("writing to file",outFile)
	pkgVersionList.extend(dependencySet)
	#pkgVersionList.append(dependencySet)
	csv.writer(outFile).writerow(pkgVersionList)
	
def getPkgVersion(extractedDir):
	return extractedDir[extractedDir.rindex('/')+1:]
	
def getPkgNameAndArchive(jsonFileName):
	returnDict = {}
	with codecs.open(jsonFileName, 'r', encoding = 'utf-8', errors='ignore') as f:
		data = f.read()
		try:
			json_data=json.loads(data)
			info = json_data['info']
			urls = json_data['urls']				
			for blocks in urls:
				if blocks['python_version']=='source':
					returnDict[blocks['filename']]=info['name']
		except:
			print("Invalid json file: ",jsonFileName,sys.exc_info()[0])
			#logging.exception('')
	return returnDict
	


def getPackageMappings(jsonDir):
	pkgMappings = {}
	#iterate all files 
	for file in os.listdir(jsonDir):
		#load json
		valDict = getPkgNameAndArchive(os.path.join(jsonDir, file))
		for k,v in valDict.items():
			pkgMappings[k] = v
			
	return pkgMappings

def mainTest():
	extractedDir = 'aiocoap'
	deps = getAllDependencies(extractedDir)
	print(len(deps))
	data = convertListToJson(deps)
	data = json.loads(data)
	print(data)
	
def convertListToJson(deps):
	jsonArray = []
	depsLen = len(deps)
	for i in range(0,depsLen):
		jsonArray.append(json.dumps(deps[i].__dict__))
#		if i < depsLen - 1:
#			jsonArrayStr+=','
	#jsonArrayStr += ''
	return jsonArray

def getGitURL (repoName):
	url = 'git://github.com/'
	return url+repoName;

def extractRepo(gitURL, repoName):
	print("Extracting repo for:",gitURL)
	try:
		folder = tf.mkdtemp(prefix='__R__'.join(repoName.split('/')))
		#folder = os.makedirs('__R__'.join(repoName.split('/')))
	except:
		print("Error creating temp folder")
		logging.exception('')
		return None,None

	try:
		repo=pygit2.clone_repository(gitURL,folder,bare=False)
	except:
		shutil.rmtree(folder)
		print("Error git clone:",gitURL)
		logging.exception('')
		return None,None
	return repo,folder
def getListOfRepos(csvFile):
	print("reading csv file")
	packageNames = []
	gitRepos = []
	with open(csvFile) as curFile:
		count = 0
		
		curReader = csv.reader(curFile)
		for row in curReader:
			count+=1
			if count == 1:
				continue
			pkgName = row[0].strip()
			gitRepo = row[1].strip()
			if not pkgName or not gitRepo:
				continue
			packageNames.append(pkgName)
			gitRepos.append(gitRepo)
	return packageNames,gitRepos

def main():
	tempDir='./' #no tmpfs

	#tempDir = './'
	tf.tempdir=tempDir
		
	args=getArgs()
	csvFile = args.inputFile
	
	outFile = open("pypi_dependencies_v4.csv",'a')
	
	packageNames,gitRepos = getListOfRepos(csvFile)
	print ("Total repos found:", len(gitRepos))
	
	start = 0
	if args.repoToContinue:
		print("continuing from repo:",args.repoToContinue)
		if args.repoToContinue in gitRepos:
			start = gitRepos.index(args.repoToContinue)
	
	myDict = {}
	count = 0
	
	for i in range(start,len(gitRepos)):

		gitURL = getGitURL(gitRepos[i])
		repo,extractedDir= extractRepo(gitURL,gitRepos[i])
		if repo==None:
			continue
		try:
			print("successfully extracted:",repo,"to:",extractedDir)
			count+=1
			dependencySet = getAllDependencies(extractedDir)
			dependencySet = convertListToJson(dependencySet)
			writeToFile([packageNames[i],gitRepos[i]],dependencySet,outFile)			
		except:
			logging.exception('')
			
		shutil.rmtree(extractedDir)
	

if __name__ == "__main__":
	main()

