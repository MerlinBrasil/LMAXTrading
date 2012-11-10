'''
Formats data contained in obevents.txt for use with spreadsheets 
and data analysis tools and saves the result in separate
files (one file per instrument) in data/



TODO 
    1/ delete data in obevents.txt once process completes (ensure first that
    no conflict arise potentially running parallel processes like LmaxTrading.java's
    own file writing process on obevents.txt during live sessions


@author: jule64@gmail.com
'''
import re
import string
import datetime
import time
import os
from symbol import if_stmt


class processdata():
    
    
    def extractheaders(self):
    # extract headers
    # run below only for the first line in the file
        if self.headerflag:
            
            # extract headers
            linesp = re.findall(r'(\b[\w-]+)=', self.line)
        
            # merge headers into one string
            finstrg = ''
            for strg in linesp:
                finstrg += strg+','
            
            # remove crappy comma at the end
            self.linetowrite = finstrg[:len(finstrg)-1]
            
            self.writetofile()
            
            self.headerflag = False
        
               
        
        
        
 
    def extractvalues(self):
        
        # remove single quotes
        self.line = re.sub(r'\'', '', self.line)
        


            
    
    # extract values
    # run below for ALL the lines in the file
    
        # extract values
        patfind = re.compile(r'=([\w\d\.\/]+)\b')
        linesp = re.findall(patfind, self.line)
    
        # merge values into one string
        finstrg = ''
        for strg in linesp:
            finstrg += strg+','
        # remove crappy comma at the end
        self.linetowrite = finstrg[:len(finstrg)-1]

        self.writetofile()



            
    def keepheadersandvaluesonly(self):
                # remove crap at beginning and end
                self.line = re.sub(r'OrderBookEventImpl{', '', self.line)
                self.line = re.sub(r'}, timereceived', ', timereceived', self.line)
                
                # remove single quotes
                self.line = re.sub(r'\'', '', self.line)
                
                
                # extract first part of string
                part1 = re.findall(r'.*bidPrices', self.line)[0]
                part1 = re.sub('bidPrices', '', part1)
                
                # extract second part of string        
                part2 = re.findall(r'bidPrices.*\], askPrices', self.line)[0]
                part2 = re.sub('askPrices', '', part2)
                part2 = re.sub('bidPrices=\[', '', part2)        
                part2 = re.sub('PricePoint{', '', part2)        
                part2 = re.sub('price', 'bid-price', part2)
                part2 = re.sub('quantity', 'bid-quantity', part2)
                part2 = re.sub('}', '', part2)
                part2 = re.sub('\]', '', part2)
                
                # extract third part of string
                part3 = re.findall(r'askPrices.*', self.line)[0]
                part3 = re.sub('askPrices=\[', '', part3)        
                part3 = re.sub('PricePoint{', '', part3)        
                part3 = re.sub('price', 'ask-price', part3)
                part3 = re.sub('quantity', 'ask-quantity', part3)
                part3 = re.sub('}', '', part3)
                part3 = re.sub('\]', '', part3)        
                
                # merge all sub strings together
                self.line = part1 + part2 + part3
                
#                print self.line
#            
#            #write result to file
#            instrufile.write(line)




    def processfile(self):
        instruidlist = []
        self.filelist = []
        
        

        mainfile = open("/Users/julienmonnier/workspace/LMAXTrading/obevents.txt", "r")
        self.line = mainfile.readline()
        
        

        # main loop.  We are reading the content of mainfile line by line so as
        # to reduce memory footprint and increase reading speed
        while self.line:
            
            print self.line
            #store content of file in variable
            #TODO check that there is no size limit for the variable
            
            idpat = re.compile(r'\d+')

            # Extract security ID of the security
            self.instruid = re.search(idpat, self.line).group()
            # check if instru ID already in list and if
            # not add it and create a new file in data
            # folder with name of self.instruid
            if self.instruid not in instruidlist:
                instruidlist.append(self.instruid)
                # create file (if doesn't exist from previous runs
                # the w+ ensures any data from previous runs is overwritten 
                instrufile = open('/Users/julienmonnier/workspace/LMAXTrading/data/'+self.instruid+'.txt', 'w+')
                # add newly open file to a list for data processing in next step
                # note we add a key value for faster retrieval during data processing
                # steps
                self.filelist.append([self.instruid,instrufile])
                self.headerflag = True

            
            
#            self.keepheadersandvaluesonly()

            self.extractheaders()
            self.extractvalues()
            
            
            
                      
#            # write the current line to the relevant file based on the instrument id
#            for instrufile in self.filelist:
#                if instrufile[0] == (self.instruid):
#                    instrufile[1].write(self.line+'\n')
            
            # load next line in main file. if no more lines the while
            # loop exits
            self.line = mainfile.readline()
            
    
        # last step:
        # closing all open files
        for instrufile in self.filelist:
            instrufile[1].close()
            
        mainfile.close()
        

    
    def writetofile(self):
        # write the current line to the relevant file based on the instrument id
        for instrufile in self.filelist:
            if instrufile[0] == (self.instruid):
                instrufile[1].write(self.linetowrite+'\n')       
    
        


if __name__ == '__main__':
    processdata().processfile()



#    line = '''instrumentId=4001, valuationBidPrice=1.26981, timeStamp=1352467491695, valuationAskPrice=1.26984, lastMarketClosePrice=1.27466, bid-price=1.26981, bid-quantity=275, bid-price=1.2698, bid-quantity=450, bid-price=1.26979, bid-quantity=1200, bid-price=1.26978, bid-quantity=375, bid-price=1.26976, bid-quantity=250, ask-price=1.26984, ask-quantity=325, ask-price=1.26985, ask-quantity=250, ask-price=1.26986, ask-quantity=1250, ask-price=1.26987, ask-quantity=100, ask-price=1.26988, ask-quantity=775, lastTradedPrice=1.26982, dailyHighestTradedPrice=1.27906, dailyLowestTradedPrice=1.269, marketClosePriceTimeStamp='1352412000000', timereceived='1352467492255', instruname=EUR/USD, obeventrow=1'''
#    print re.findall(r'(\b[\w-]+)=', line)

    


















        
