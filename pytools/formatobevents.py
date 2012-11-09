'''
Formats the data in obevents.txt for use with spreadsheets 
and data analysis tools and saves the result in /data/fileout.txt



TODO 
    1/ auto rename fileout.txt with name provided as second (optional) argument
in script and add date time at the beginning of the file name
    2/ delete data in obevents after file has been succesfully output to outfile.txt
    (ensure it does not conflict with potential parallel processes that try to write to
    obevents (like LmaxTrading.java)


@author: jule64@gmail.com
'''
import re





if __name__ == '__main__':
    with open("/Users/julienmonnier/workspace/LMAXTrading/obevents.txt", "r") as sources:
        lines = sources.readlines()
    
    with open("/Users/julienmonnier/workspace/LMAXTrading/data/fileout.txt", "w") as sources:
        for line in lines:
            # remove crap at beginning and end
            line = re.sub(r'OrderBookEventImpl{', '', line)
            line = re.sub(r'}, timereceived', ', timereceived', line)
            
            
            # extract first part of string
            part1 = re.findall(r'.*bidPrices', line)[0]
            part1 = re.sub('bidPrices', '', part1)
            
            # extract second part of string        
            part2 = re.findall(r'bidPrices.*\], askPrices', line)[0]
            part2 = re.sub('askPrices', '', part2)
            part2 = re.sub('bidPrices=\[', '', part2)        
            part2 = re.sub('PricePoint{', '', part2)        
            part2 = re.sub('price', 'bid-price', part2)
            part2 = re.sub('quantity', 'bid-quantity', part2)
            part2 = re.sub('}', '', part2)
            part2 = re.sub('\]', '', part2)
            
            # extract third part of string
            part3 = re.findall(r'askPrices.*', line)[0]
            part3 = re.sub('askPrices=\[', '', part3)        
            part3 = re.sub('PricePoint{', '', part3)        
            part3 = re.sub('price', 'ask-price', part3)
            part3 = re.sub('quantity', 'ask-quantity', part3)
            part3 = re.sub('}', '', part3)
            part3 = re.sub('\]', '', part3)        
            
            # merge all sub strings together
            line = part1 + part2 + part3 +'\n'
            
            #write result to file
            sources.write(line)
        
        
