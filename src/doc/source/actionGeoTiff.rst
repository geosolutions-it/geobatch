GeoTIFF Action
==============

The GeoTIFF Action is used for GeoTIFF raster data. It can retile, generate overviews, and publish in GeoServer.

Publishing in GeoServer
-----------------------

Defined as a ``<GeoServerActionConfiguration>`` with the ``serviceID`` being ``GeotiffGeoServerService``.

* **Input**: A collection of GeoTIFF files.
* **Output**: The collection of successfully published GeoTIFF files.
* **Actions**: Create a coverageStore for the GeoTIFF, upload data if needed, publish new layer, apply the default style.
* **Sample configuration**:

.. sourcecode:: xml

    <GeoServerActionConfiguration>
        <serviceID>GeotiffGeoServerService</serviceID>
        
        <id>geotiff</id>
        <description>Action to ingest GeoTIFF on GeoServer</description>
        <name>geotiff action</name>

        <listenerId>GeoTIFFStatusActionLogger0</listenerId>
        <listenerId>GeoTIFFActionLogger0</listenerId>

        <geoserverPWD>geoserver</geoserverPWD>
        <geoserverUID>admin</geoserverUID>
        <geoserverURL>http://localhost:8080/geoserver</geoserverURL>
        
        <dataTransferMethod>EXTERNAL</dataTransferMethod>
        <defaultNamespace>it.geosolutions</defaultNamespace>
        <defaultStyle>raster</defaultStyle>
        <crs>EPSG:4326</crs>
        <envelope />
        <styles />
    </GeoServerActionConfiguration>

Retiling
--------

Defined as a ``<GeotiffRetilerConfiguration>`` with the ``serviceID`` being ``GeotiffRetilerService``.

* **Input**: A collection of GeoTIFF files, or a directory containing GeoTIFF files. *GeoBatch* must have write access to the files and directory.
* **Output**: The collection of successfully retiled GeoTIFF files, or the directory.
* **Actions**: Produce tiled GeoTIFF files.
* **Sample configuration**:

.. sourcecode:: xml

    <GeotiffRetilerConfiguration>
        <serviceID>GeotiffRetilerService</serviceID>
        
        <id>GeoTiffRetiler</id>
        <description>Action to retile a geotif</description>
        <name>GeoTiffRetiler action</name>

        <listenerId>GeoTIFFStatusActionLogger0</listenerId>
        <listenerId>GeoTIFFActionLogger0</listenerId>

        <forceToBigTiff>false</forceToBigTiff>
        <tileH>256</tileH>
        <tileW>256</tileW>
    </GeotiffRetilerConfiguration>
    
Significant parameters are ``tileH`` and ``tileW``, which are the tile dimensions in pixels, and ``forceToBigTiff``.


Embedding Overviews
-------------------

Defined as a ``<GeotiffOverviewsEmbedderConfiguration>`` with the ``serviceID`` being ``GeotiffOverviewsEmbedderService``.

* **Input**: A collection of GeoTIFF files, or a directory containing GeoTIFF files. *GeoBatch* must have write access to the files and directory.
* **Output**: The collection of successfully treated GeoTIFF files, or the directory.
* **Actions**: Embed overviews into GeoTIFF files.
* **Sample configuration**:

.. sourcecode:: xml

    <GeotiffOverviewsEmbedderConfiguration>
        <serviceID>GeotiffOverviewsEmbedderService</serviceID>
        
        <id>GeoTiffOverviewsEmbedder</id>
        <description>Action to add overviews to a geotif</description>
        <name>GeotiffOverview embedder action</name>

        <listenerId>GeoTIFFStatusActionLogger0</listenerId>
        <listenerId>GeoTIFFActionLogger0</listenerId>

        <downsampleStep>2</downsampleStep>
        <scaleAlgorithm>Average</scaleAlgorithm>
        <numSteps>15</numSteps>
        <tileH>256</tileH>
        <tileW>256</tileW>
    </GeotiffOverviewsEmbedderConfiguration>
    
Significant parameters are:

* ``numSteps``: the number of overview scales to generate,
* ``downsampleStep``: the 'scale factor' between two consecutive steps,
* ``scaleAlgorithm``: one of *Nearest*, *Bilinear*, *Bicubic*, *Average* or *Filtered*),
* ``tileH`` and ``tileW``.

