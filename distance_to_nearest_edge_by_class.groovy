//Building off of multiple scripts, hurrah for the forum!
// https://forum.image.sc/t/qupath-distance-between-annotations/47960/10?u=mike_nelson

//get an object - this could be made a loop.
centralObject = getSelectedObject()
//get the geometry of that object
centralObjectGeom = getSelectedObject().getROI().getGeometry()
//get the boundary of the object or turn it into lines
centralObjectBoundary = centralObjectGeom.getBoundary()

//Other stuff
def cal = getCurrentServer().getPixelCalibration()
if (cal.pixelWidth != cal.pixelHeight) {
    println "Pixel width != pixel height ($cal.pixelWidth vs. $cal.pixelHeight)"
    println "Distance measurements will be calibrated using the average of these"
}


//check the distance to every other object

objectsToCheck = getAllObjects().findAll{it.isDetection() || it.isAnnotation()}
objectsToCheck.each{ o ->
    currentGeom = o.getROI().getGeometry()
    double distancePixels = centralObjectBoundary.distance(currentGeom)
    double distanceCalibrated = distancePixels * cal.getAveragedPixelSize()
    if (centralObjectGeom.contains(currentGeom)){
        o.getMeasurementList().putMeasurement("Distance in um to nearest "+centralObject.getPathClass()+" annotation", -distanceCalibrated)   
    }else{
        o.getMeasurementList().putMeasurement("Distance in um to nearest "+centralObject.getPathClass()+" annotation", distanceCalibrated)   
    }
}
