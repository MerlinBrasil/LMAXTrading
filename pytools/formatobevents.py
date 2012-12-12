

"""Script for formating data contained in obevents.txt into a 
spreadsheet-friendly format and saves the result in separate
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


The formatted data is stored in: /data/'instrumentid'.txt

NOTE on performance:  current performance tuning tests indicate an
average process time of 0.3 milliseconds (0.0003s) per line of data.
This performance implies an expected process time of about 2 minutes
for a file containing 360,000 rows of data (which corresponds to the average
number of tick data received during the course of a normal trading day using
current standard LMAX API access)


@author: jule64@gmail.com
"""


#TODO 
#    1/ delete data in obevents.txt once process completes (ensure first that
#    no conflict arise potentially running parallel processes like LmaxTrading.java's
#    own file writing process on obevents.txt during live sessions



import re
import time


class ProcessData():

    """Contains static methods for formatting the obevents.txt data
    Currently the class contains one public method, processfile().
    The other methods in this class have been prefixed with an '_'
    to signal that they should not be called by the client code
    (although they can as any good Python programmer knows)
    """
    
    # main method
    @staticmethod
    def processfile(cls,timeproc=False):
        cls.instruidlist = []
        cls.filelist = []
        
        cls.timeproc = timeproc
        cls.timeelepsed = []

        with open("/Users/julienmonnier/workspace/LMAXTrading/obevents.txt", "r") as mainfile:
            
            cls.line = mainfile.readline()
            
            # main loop.  We are reading the content of mainfile line by line and process
            # formatting statements on it before writing the final result to the relevant
            # instrument file
            # NOTE: using a while statement (as opposed to a readlines()) allows to reduce
            # memory footprint and speed up the overall process
            while cls.line:
                
                # save current time for performance monitoring
                cls._starttime = time.time()
                
                cls._open_instrument_file()            
                cls._keep_headervalue_pairs_only()
                if cls.headerflag:
                    cls._extractheaders()
                    cls._writetofile()
                cls._extractvalues()
                cls._writetofile()
                cls.line = mainfile.readline()
                
                # save current time for performance monitoring
                cls._endtime = time.time()
                
                # add elapsed time to list for future use
                cls.timeelepsed.append(cls._endtime - cls._starttime)
            
            # close all open files
            for instrufile in cls.filelist:
                instrufile[1].close()
            
            # display time statistics if requested
            if cls.timeproc:
                print sum(cls.timeelepsed) / len(cls.timeelepsed)
                
    # =================================================
    # =================================================

    # opens a new instrument file based on value of instrument ID retrieved from
    # cls.line.  Does nothing if file already open
    @staticmethod
    def _open_instrument_file(cls):
        # Extract security ID from cls.line and save to class instance for later use
            cls.instruid = re.search(r'\d+', cls.line).group()
            # check if security ID is already in list and if
            # not add it and create a new file in data
            # folder with name of cls.instruid
            if cls.instruid not in cls.instruidlist:
                cls.instruidlist.append(cls.instruid)
                # create file (if doesn't exist from previous runs
                # the w+ ensures any data from previous runs is overwritten 
                instrufile = open('/Users/julienmonnier/workspace/LMAXTrading/data/'+cls.instruid+'.txt', 'w+')
                # add newly open file to a list for data processing in next step
                # note we add a key value for faster retrieval during data processing
                # steps
                cls.filelist.append([cls.instruid,instrufile])
                cls.headerflag = True
    
    # does what it says on the tin
    @staticmethod
    def _keep_headervalue_pairs_only(cls):
        
        # regexp to split line's data into named groups 
        p = re.compile(r'(?P<start>^.*)(?P<bid>[[][^]]*[]])(?P<middle>.*)(?P<ask>[[][^]]*[]])(?P<end>.*$)')
        
        # add bid prefix to bid prices and quantities and do the same for ask prices and quantities
        cls.line = (p.search(cls.line).group('start')
            + re.sub(r'quantity','bid-quantity',re.sub(r'price','bid-price',p.search(cls.line).group('bid')))
            + p.search(cls.line).group('middle')
            + re.sub(r'quantity','ask-quantity',re.sub(r'price','ask-price',p.search(cls.line).group('ask')))
            + p.search(cls.line).group('end'))
        
        # remove all non necessary info contained in cls.line in order to keep only pairs of 'header=value'
        cls.line = re.sub(r'OrderBookEventImpl|bidPrices=|askPrices=|PricePoint|[[\]{}\']','',cls.line)



    # extracts headers
    @staticmethod
    def _extractheaders(cls):
        
        # extract headers
        linesp = re.findall(r'(\b[\w-]+)=', cls.line)
    
        # merge headers into one string
        finstrg = ''
        for strg in linesp:
            finstrg += strg+','
        
        # remove comma at the end and save to variable
        cls.linetowrite = finstrg[:len(finstrg)-1]

        cls.headerflag = False
        
        
    # extracts values
    @staticmethod
    def _extractvalues(cls):
        
        # extract values
        patfind = re.compile(r'=([\w\d\.\/]+)\b')
        linesp = re.findall(patfind, cls.line)
    
        # merge values into one string
        finstrg = ''
        for strg in linesp:
            finstrg += strg+','
        # remove comma at the end and save to variable
        cls.linetowrite = finstrg[:len(finstrg)-1]


    # write formatted line of data into the relevant file based on value of instrument id
    @staticmethod
    def _writetofile(cls):
        for instrufile in cls.filelist:
            if instrufile[0] == (cls.instruid):
                instrufile[1].write(cls.linetowrite+'\n')



if __name__ == '__main__':
    ProcessData.processfile(True)

            







        
