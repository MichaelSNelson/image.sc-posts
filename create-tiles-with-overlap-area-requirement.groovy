//Adjustment of stitching script to create overlapping rectangular tiles for testing
//Current script has tile export commented out, though will still generate a text file of tile coordinates
//Results are placed in a TileExport folder within a project. Without a project this script will fail
//Michael S. Nelson 28062021

double frameWidth = 3000
double frameHeight = 3000
//Discard tiles with less than 20% overlap with the main annotation, for instance
double discardTilePercent = 20
//This is in PERCENT overlap between tiles, do not put 0.1 for 10%
double overlapPercent = 0

pixelSize = getCurrentImageData().getServer().getPixelCalibration().getAveragedPixelSize()
baseDirectory = path = buildFilePath(PROJECT_BASE_DIR, 'TileExport')

clearDetections()
//Potentially store tiles as they are created
newTiles = []

//Store XY coordinates in an array
xy = []
//Check all annotations. Use .findAll{expression} to select a subset
annotations = getAnnotationObjects()
server = getCurrentServer()
imageName = GeneralTools.getNameWithoutExtension(getCurrentServer().getMetadata().getName())
//Ensure the folder to store the csv exists
tilePath = buildFilePath(baseDirectory, "Tiles csv")
mkdirs(tilePath)
imagePath = buildFilePath(baseDirectory, "Image tiles")
mkdirs(imagePath)
//CSV will be only two columns with the following header
String header="dim = 2";
path = buildFilePath(baseDirectory, "Tiles csv", "TileConfiguration.txt")
int index = 0
new File(path).withWriter { fw ->
    fw.writeLine(header)
    //Make sure everything being sent is a child and part of the current annotation.

    annotations.each{a->
        roiA = a.getROI()
        //generate a bounding box to create tiles within
        bBoxX = a.getROI().getBoundsX()
        bBoxY = a.getROI().getBoundsY()
        bBoxH = a.getROI().getBoundsHeight()
        bBoxW = a.getROI().getBoundsWidth()
        y = bBoxY
       
        while (y< bBoxY+bBoxH){
            x = bBoxX
            while (x < bBoxX+bBoxW){
                
                xy << [x,y]
                //create the rectangle object for reference
                def roi = new RectangleROI(x,y,frameWidth,frameHeight, ImagePlane.getDefaultPlane())
                if(roiA.getGeometry().intersects(roi.getGeometry())){
                    if(GeometryTools.geometryToROI(roiA.getGeometry().intersection(roi.getGeometry()), ImagePlane.getDefaultPlane()).getArea() > frameWidth*frameHeight*discardTilePercent/100){
                        newAnno = PathObjects.createAnnotationObject(roi)
                        newAnno.setName(index.toString())
                        newTiles << newAnno
                    
                     //Create a text file with the coordinates for stitching
                        String line = index +".tiff; ; ("+x+", "+y+")" 
                        fw.writeLine(line)
                        //pathToImage = buildFilePath(baseDirectory, "Image tiles", index+".tiff")
                        //requestROI = RegionRequest.createInstance(server.getPath(), 1, roi)
                        //writeImageRegion(server, requestROI, pathToImage)
                        index++
                    }
                }
                x = x+frameWidth-overlapPercent/100*frameWidth
            }
            y = y+frameHeight-overlapPercent/100*frameHeight
            
        }
    }


}
print newTiles
//Comment out to avoid visual tiles.
addObjects(newTiles)
resolveHierarchy()

print " "
print "Output saved in  folder at " + tilePath
print "done"


import qupath.lib.regions.ImagePlane
import qupath.lib.roi.RectangleROI;
