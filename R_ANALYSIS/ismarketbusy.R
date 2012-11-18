

alertmarket <- function(fseries,pseries) {
manipulate(alertmarketimpl(fseries,pseries, restthreshold,rollingavg,restfactor,restthresholdlistsize, busyfactor),
           restthreshold = slider(100,4000,initial=360),rollingavg = slider(2,50,initial=22),
           restfactor = slider(20,100,initial=85),
           restthresholdlistsize = slider(10,100,initial=30), busyfactor = slider(10,100,initial=30))
}


alertmarketimpl <- function(fseries,pseries, restthreshold = 400,rollingavg = 25,restfactor = 85,restthresholdlistsize = 30, busyfactor = 40) {
  
  zoo.fs = zoo(fseries)
  zoo.ps = zoo(pseries)
  
  zoo.fs.diff = diff(zoo.fs,lag=1)
  zoo.avg.fs.diff = rollmean(zoo.fs.diff,k=rollingavg)
  
  # for testing purpose only
  # zoo.avg.fs.diff[3000:3500] = zoo.avg.fs.diff[100:600]

  

  listalert = c()
  
  busythreshold = floor((busyfactor/100) * restthreshold)
  

  restthresholdlist = 0
  restthresholdlist[1:restthresholdlistsize] <- 0
  isreadyrestthreshold = FALSE
  isrested = FALSE
  israisealert = FALSE

  displaycounter = 0
  
  iter = 0

  print(paste("busythreshold is: ",busythreshold))
  print(paste("restthreshold is: ",restthreshold))
  print(paste("zoo.avg.fs.diff length is: ",length(zoo.avg.fs.diff)))
  

      
  for(i in zoo.avg.fs.diff) {
    
    iter = iter + 1
    
    # determine if zoo.avg.fs.diff has spent long
    # enough above the restthreshold
    
    if(i>restthreshold){
      
      # add 1 to restthresholdlist
      restthresholdlist[(iter%%restthresholdlistsize)+1] = 1
#       restcounter = restcounter + 1
      
    } else {
      
      restthresholdlist[(iter%%restthresholdlistsize)+1] = 0
      
    }

    # series is considered rested when at least x% of its past n values were above
    # the restthreshold
    # n is the size of restthresholdlist (30 should be a good enough size)
    # x is the restfactor (85% should be a good enough factor)
    if (!((sum(restthresholdlist)/length(restthresholdlist))<(restfactor/100)))
      isrested = TRUE
      
    
    # if the series is rested then raise alert
    # if series dropps below frequency floor
    
    if(isrested == TRUE && !(i > busythreshold)) {
      
      
      ismarketbusy = TRUE
      
      
      
      # reset resting variables
      isrested = FALSE
      restthresholdlist[1:restthresholdlistsize] <- 0
      
      
    } else {
      
      ismarketbusy = FALSE
      
    }
    
    
    
    if(ismarketbusy == TRUE) {
      israisealert = TRUE
    }
    
    
    
    if(israisealert == TRUE && !(displaycounter > 15)){
#       print(paste("alert is on. iter is: ",iter))
      alert = 1
      displaycounter = displaycounter + 1

    } else {
      
      alert = 0
      israisealert = FALSE
      displaycounter = 0
      
    }


    
    listalert = c(listalert,alert) 

 
  }

  print(length(listalert))
  print(length(zoo.avg.fs.diff))
  
  par(mfrow = c(2,1),mar=c(1,4,1,0.5), oma=c(1,1,3,1),cex=.6)
  plot(zoo.ps[rollingavg:length(zoo.ps)],type='l',col="red",xaxt='n')
  plot(zoo.avg.fs.diff,type='l',col="green")
  par(new = TRUE)
  plot(listalert,type="h",yaxt='n')

}
