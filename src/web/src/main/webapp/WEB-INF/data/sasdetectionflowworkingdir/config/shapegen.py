#------------------------------------------------
# Utility script which takes detection matfile
# as input and produces a shapefile for it.
#
# It also writes a PRJ with the WKT in case the
# proper definition is available in the specified
# dictionary (The PRJ dictionary folder is passed
# as third argument to the script). In case it 
# is missing, simply write out the "EPSG:XXXcode"
#
# CRS definitions are defined as file with name:
# crsXXX.prj where XXX is the EPSG code. As an 
# instance, crs4326.prj should contain the WKT
# of the EPSG:4326 CRS definition ( = WGS84)
#------------------------------------------------
import os.path
import shutil
import scipy.io
import sys
from osgeo import ogr
from osgeo import osr
from optparse import OptionParser
from time import strftime

parser = OptionParser()
parser.add_option("-i", "--input", dest="input",
                  help="Input mission detections folder", metavar="FILE")
parser.add_option("-o", "--outdir", dest="outdir",
                  help="Write shapefile to that output dir")
parser.add_option("-c", "--crsdir", dest="crsdir", default=None, 
                  help="Folder containing CRS definitions")
parser.add_option("-l", "--logdir", dest="logdir", default=None, 
                  help="Folder containing Logging")                  

(options, args) = parser.parse_args()
if options.input is None:
  print ("Input mission detections folder is missing. use -h for the help")
  sys.exit(0)
elif options.outdir is None:
  print ("Output dir is missing. use -h for the help")
  sys.exit(0)


inputDir = options.input
logOps = False
logFile =""

if (not os.path.isdir(inputDir)):
  print ("Specified Input path is not a folder. use -h for the help")
  sys.exit(0)
dirList = os.listdir(inputDir)

shp_path = options.outdir
print(shp_path)
crs_path = options.crsdir
  
#---------------------------------
# Getting the data name
#---------------------------------
baseDir = os.path.basename(inputDir)
baseDir = "target_" + baseDir

#---------------------------------
# Preparing the output folder
#---------------------------------
shp_filepath = os.path.join(shp_path, baseDir)
shapeDir = shp_filepath + os.sep
if (not os.path.isdir(shapeDir)):
	os.mkdir(shapeDir)
shp_filepath = (shp_filepath + os.sep + baseDir + ".shp")

if options.logdir is None:
  logDir = os.path.join(shp_path, "logs")
  print(logDir)
else:
  logDir = options.logdir
  print(logDir)

if (not os.path.isdir(logDir)):
  os.mkdir(logDir)
logOps = True
time = strftime("%Y-%m-%d-%H%M%S")
logFile = os.path.join(logDir, time + "-" + baseDir + ".log")
logg = open(logFile,'w')
logg.write("Extracting detections from input dir " + inputDir + " to " + shp_filepath + "\n")

drv = ogr.GetDriverByName('ESRI Shapefile')
ds = drv.CreateDataSource(shp_filepath)

first = True
  #---------------------------------
  # Preparing fields
  #---------------------------------


for fname in dirList:
  if (not fname.endswith(".mat")):
    continue
  if (fname.find("_det") == -1):
    continue
  baseName = os.path.basename(fname)
  
  #----------------------------------
  # Getting the original raw tilename
  #----------------------------------
  index = 0
  length = len(baseName)
  for i in range (0,10):
    index = baseName.find('_',index+1,length)
  name = baseName[0:index]

  #---------------------------------
  # Getting matlab entries
  #---------------------------------
  matFile = os.path.join(inputDir, fname)
  if logOps:
    logg.write("Processing file: " +matFile + "\n")
  
  detection = scipy.io.loadmat(matFile, struct_as_record=True)

  # Scores
  d_score0 = detection['d_score0']
  d_score = detection['d_score']
  d_score_DW2 = detection['detection_score_DW2']

  # Coordinates
  northings_llc = detection['northings_llc'][0][0]
  northings_urc = detection['northings_urc'][0][0]
  eastings_llc = detection['eastings_llc'][0][0]
  eastings_urc = detection['eastings_urc'][0][0]
  northings_lrc = detection['northings_lrc'][0][0]
  northings_ulc = detection['northings_ulc'][0][0]
  eastings_lrc = detection['eastings_lrc'][0][0]
  eastings_ulc = detection['eastings_ulc'][0][0]
  lat_target = detection['lat_target']
  lon_target = detection['lon_target']

  # UTM Zone
  uzone = detection['uzone']
  layername = "it.geosolutions:"+name.lower()

  #---------------------------------
  # Preparing the BoundingBox
  #---------------------------------
  minX = min(eastings_llc,eastings_ulc,eastings_urc,eastings_lrc)
  maxX = max(eastings_llc,eastings_ulc,eastings_urc,eastings_lrc)
  minY = min(northings_llc,northings_ulc,northings_urc,northings_lrc)
  maxY = max(northings_llc,northings_ulc,northings_urc,northings_lrc)
  x0, y0, x1, y1 = str(minX), str(minY), str(maxX), str(maxY)

  
  if (first):
    #---------------------------------
    # Setting CRS
    #---------------------------------
    utmzone = uzone[0]
    zone = utmzone[0:2]
    vzone = utmzone[2:3]
    if ord(vzone) < 77:
      tzone = '7'
    else:
      tzone = '6'
    code = '32' + tzone +zone
    epsgcode = 'EPSG:' + code
    t_srs = osr.SpatialReference()
    t_srs.SetFromUserInput(str(epsgcode))

    layer = ds.CreateLayer(ds.GetName(), geom_type = ogr.wkbPolygon, srs = t_srs)
    layer.CreateField(ogr.FieldDefn('d_score', ogr.OFTReal))
    layer.CreateField(ogr.FieldDefn('d_score0', ogr.OFTReal))
    layer.CreateField(ogr.FieldDefn('d_scoreDW2', ogr.OFTReal))
    layer.CreateField(ogr.FieldDefn('lon_target', ogr.OFTReal))
    layer.CreateField(ogr.FieldDefn('lat_target', ogr.OFTReal))
    layer.CreateField(ogr.FieldDefn('layername', ogr.OFTString))
    layer.CreateField(ogr.FieldDefn('minx', ogr.OFTReal))
    layer.CreateField(ogr.FieldDefn('miny', ogr.OFTReal))
    layer.CreateField(ogr.FieldDefn('maxx', ogr.OFTReal))
    layer.CreateField(ogr.FieldDefn('maxy', ogr.OFTReal))
    first = False

  geom = ogr.Geometry(type = layer.GetLayerDefn().GetGeomType())
  geom.AssignSpatialReference(t_srs)
  wkt = 'POLYGON(('+x0+' '+y0+','+x0+' '+y1+','+x1+' '+y1+','+x1+' '+y0+','+x0+' '+y0+'))'
  geom2 = ogr.CreateGeometryFromWkt(wkt)
  geom = geom2;

  #---------------------------------
  # Setting feature fields
  #---------------------------------
  feat = ogr.Feature(feature_def = layer.GetLayerDefn())
  feat.SetGeometryDirectly(geom)
  feat.SetField('d_score', d_score[0][0])
  feat.SetField('d_score0', d_score0[0][0])
  feat.SetField('d_scoreDW2', d_score_DW2[0][0])
  feat.SetField('lat_target', lat_target[0][0])
  feat.SetField('lon_target', lon_target[0][0])
  feat.SetField('layername', layername)
  feat.SetField('minx', minX)
  feat.SetField('miny', minY)
  feat.SetField('maxx', maxX)
  feat.SetField('maxy', maxY)
  layer.CreateFeature(feat)
  feat.Destroy()
  if (logOps):
    logg.write("Feature added \n")
  

#--------------------------------------------------
# Updating PRJ with OGC WKT instead of ESRI
#   -------------------------------------
# In case a valid PRJ for this EPSG code exists,
# write the WKT. Otherwise, write the "EPSG:XXXcode"
#--------------------------------------------------
outprj = shp_filepath[0:len(shp_filepath)-4]+".prj"
crsfile = "crs" + code + ".prj"
found = False
if(not crs_path is None):
  crspath = os.path.join(crs_path, crsfile)
  if (os.path.isfile(crspath)):
    found = True
if (found):
  shutil.copy(crspath, outprj)
else:
  f = open(outprj,'w')
  f.write(epsgcode)
  f.close()
  
if logOps:
  logg.write("Extraction done \n")
  logg.close()

