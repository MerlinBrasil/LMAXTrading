'''
Formats data contained in obevents.txt for use with spreadsheets 
and data analysis tools and saves the result in separate
files (one file per instrument)


The data in obevents.txt has the following format:

#    OrderBookEventImpl{instrumentId=4001, valuationBidPrice=1.27216, timeStamp=1352483961316, valuationAskPrice=1.2722
#    , lastMarketClosePrice=1.27466, bidPrices=[PricePoint{price=1.27216, quantity=150}, PricePoint{price=1.27215, quantity=260}
#    , PricePoint{price=1.27214, quantity=300}, PricePoint{price=1.27213, quantity=100}, PricePoint{price=1.27212, quantity=1300}]
#    , askPrices=[PricePoint{price=1.2722, quantity=300}, PricePoint{price=1.27221, quantity=50}, PricePoint{price=1.27222, quantity=250}
#    , PricePoint{price=1.27223, quantity=250}, PricePoint{price=1.27225, quantity=1150}], lastTradedPrice=1.27217
#    , dailyHighestTradedPrice=1.27906, dailyLowestTradedPrice=1.269, marketClosePriceTimeStamp='1352412000000'}
#    , timereceived='1352483961889', instruname=EUR/USD, obeventrow=1


The formatted data looks like this:

#    instrumentId,valuationBidPrice,timeStamp,valuationAskPrice,lastMarketClosePrice,bid-price,bid-quantity,bid-price,bid-quantity
#    ,bid-price,bid-quantity,bid-price,bid-quantity,bid-price,bid-quantity,ask-price,ask-quantity,ask-price,ask-quantity,ask-price
#    ,ask-quantity,ask-price,ask-quantity,ask-price,ask-quantity,lastTradedPrice,dailyHighestTradedPrice,dailyLowestTradedPrice
#    ,marketClosePriceTimeStamp,timereceived,instruname,obeventrow
#    4001,1.26981,1352467491695,1.26984,1.27466,1.26981,275,1.2698,450,1.26979,1200,1.26978,375,1.26976,250,1.26984,325,1.26985
#    ,250,1.26986,1250,1.26987,100,1.26988,775,1.26982,1.27906,1.269,1352412000000,1352467492255,EUR/USD,1


The formatted data is stored in: /data/



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


class processdata():
    
    
    
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

            
            
            self.keep_headervalue_pairs_only()
            
            if self.headerflag:
                self.extractheaders()
                self.writetofile()
                
            self.extractvalues()
            self.writetofile()

            

            # last statement in while loop
            # loads next line in main file into line 
            # if no more lines, while loop exits
            self.line = mainfile.readline()
        
        # first statement after while loop
        # close all open files
        for instrufile in self.filelist:
            instrufile[1].close()
        mainfile.close()

    # end of processdata()   
    # =================================================
    # =================================================

    
    
    # does what it says on the tin
    def keep_headervalue_pairs_only(self):
        
        # regexp to split line's data into named groups 
        p = re.compile(r'(?P<start>^.*)(?P<bid>[[][^]]*[]])(?P<middle>.*)(?P<ask>[[][^]]*[]])(?P<end>.*$)')
        
        # add bid prefix to bid prices and quantities and do the same for ask prices and quantities
        self.line = (p.search(self.line).group('start')
            + re.sub(r'quantity','bid-quantity',re.sub(r'price','bid-price',p.search(self.line).group('bid')))
            + p.search(self.line).group('middle')
            + re.sub(r'quantity','ask-quantity',re.sub(r'price','ask-price',p.search(self.line).group('ask')))
            + p.search(self.line).group('end'))
        
        # remove all non necessary info contained in line in order to keep only pairs of 'header=value'
        self.line = re.sub(r'OrderBookEventImpl|bidPrices=|askPrices=|PricePoint|[[\]{}\']','',self.line)



    # extracts headers
    def extractheaders(self):
        
        # extract headers
        linesp = re.findall(r'(\b[\w-]+)=', self.line)
    
        # merge headers into one string
        finstrg = ''
        for strg in linesp:
            finstrg += strg+','
        
        # remove comma at the end and save to variable
        self.linetowrite = finstrg[:len(finstrg)-1]

        self.headerflag = False
        
        
    # extracts values
    def extractvalues(self):
        
        # extract values
        patfind = re.compile(r'=([\w\d\.\/]+)\b')
        linesp = re.findall(patfind, self.line)
    
        # merge values into one string
        finstrg = ''
        for strg in linesp:
            finstrg += strg+','
        # remove comma at the end and save to variable
        self.linetowrite = finstrg[:len(finstrg)-1]


    # write formatted line of data into the relevant file based on value of instrument id
    def writetofile(self):
        for instrufile in self.filelist:
            if instrufile[0] == (self.instruid):
                instrufile[1].write(self.linetowrite+'\n')


    
    
        


if __name__ == '__main__':
    processdata().processfile()

            







        
