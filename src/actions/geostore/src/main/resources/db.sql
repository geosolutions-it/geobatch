--deleting user, category, resource and attribute tables 
DELETE FROM gs_security;
DELETE FROM gs_user;
DELETE FROM gs_stored_data;
DELETE FROM gs_attribute;
DELETE FROM gs_resource;
DELETE FROM gs_category;


-- init user

INSERT INTO gs_user values(1,'admin','Y5kHvSCN6aOA3YPxXPautA==','ADMIN',null);

--init categories

INSERT INTO gs_category values(1,'layer');
INSERT INTO gs_category values(2,'layerupdate');
INSERT INTO gs_category values(3,'statsdef');
INSERT INTO gs_category values(4,'statsdata');


--init resource of layer category

INSERT INTO gs_resource values(1, now(), '',now(), '', 'layer1',1);
INSERT INTO gs_resource values(2, now(), '',now(), '', 'layer2',1);
INSERT INTO gs_resource values(3, now(), '',now(), '', 'layer3',1);

-- init of resource of layerupdate category

INSERT INTO gs_resource values(4, now(), '',now(), '', 'layer1_2009',2);
INSERT INTO gs_resource values(5, now(), '',now(), '', 'layer1_2010_3',2);
INSERT INTO gs_resource values(6, now(), '',now(), '', 'layer1_2011',2);

INSERT INTO gs_attribute values(1,null, 'LAYERNAME',null, 'layer1','STRING', 4);
INSERT INTO gs_attribute values(2,null, 'YEAR',2009, null,'NUMBER',4);
INSERT INTO gs_attribute values(3,null, 'LAYERNAME',null, 'layer1','STRING', 5);
INSERT INTO gs_attribute values(4,null, 'YEAR',2010, null,'NUMBER',5);
INSERT INTO gs_attribute values(5,null, 'MONTH',3, null,'NUMBER',5);

INSERT INTO gs_attribute values(6,null, 'LAYERNAME',null, 'layer1','STRING', 6);
INSERT INTO gs_attribute values(7,null, 'YEAR',2011, null,'NUMBER',6);


INSERT INTO gs_resource values(7, now(), '',now(), '', 'layer2_2009',2);
INSERT INTO gs_resource values(8, now(), '',now(), '', 'layer2_2010_3',2);


INSERT INTO gs_attribute values(8,null, 'LAYERNAME',null, 'layer2','STRING', 7);
INSERT INTO gs_attribute values(9,null, 'YEAR',2009, null,'NUMBER',7);

INSERT INTO gs_attribute values(10,null, 'LAYERNAME',null, 'layer2','STRING', 8);
INSERT INTO gs_attribute values(11,null, 'YEAR',2010, null,'NUMBER',8);
INSERT INTO gs_attribute values(12,null, 'MONTH',3, null,'NUMBER',8);



INSERT INTO gs_resource values(9, now(), '',now(), '', 'layer3_2010_3',2);
INSERT INTO gs_resource values(10, now(), '',now(), '', 'layer3_2011',2);

INSERT INTO gs_attribute values(13,null, 'LAYERNAME',null, 'layer3','STRING', 9);
INSERT INTO gs_attribute values(14,null, 'YEAR',2010, null,'NUMBER',9);
INSERT INTO gs_attribute values(15,null, 'MONTH',3, null,'NUMBER',9);

INSERT INTO gs_attribute values(16,null, 'LAYERNAME',null, 'layer3','STRING', 10);
INSERT INTO gs_attribute values(17,null, 'YEAR',2011, null,'NUMBER',10);

-- init of resources of the StatsDef category

INSERT INTO gs_resource values(11, now(), '',now(), '', 'AVERAGE',3);
INSERT INTO gs_resource values(12, now(), '',now(), '', 'STDDEV',3);

INSERT INTO gs_attribute values(18,null, 'layer1',null, 'layer','STRING', 11);
INSERT INTO gs_attribute values(19,null, 'layer2',null, 'layer','STRING', 11);

INSERT INTO gs_attribute values(20,null, 'layer1',null, 'layer','STRING', 12);
INSERT INTO gs_attribute values(21,null, 'layer3',null, 'layer','STRING', 11);

--init of the statsData category

INSERT INTO gs_stored_data values(1, '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<statisticConfiguration>
    <name>area_admin_small</name>
    <title>Low resolution admin areas</title>
    <description>Compute the area for the administrative areas. Low resolutions raster.</description>

    <topic>rdc</topic>
    <topic>area</topic>
    <topic>administartive</topic>

    <stats>
        <stat>SUM</stat>
        <stat>MIN</stat>
        <stat>MAX</stat>
        <stat>COUNT</stat>
    </stats>

    <deferredMode>false</deferredMode>

    <dataLayer>
        <file>/home/geosol/data/unredd/rdc_admin_area_small.tif</file>
    </dataLayer>

    <classificationLayer zonal="true">
        <file>/home/geosol/data/unredd/rdc_limite_administrative_small.tif</file>
        <nodata>65535</nodata>
    </classificationLayer>

    <output>
        <format>CSV</format>
        <separator>;</separator>
        <NaNValue>-1</NaNValue>
    </output>
<!--    <classifications>
        <filename>filename2</filename>
        <pivot>
            <value>0.0</value>
            <value>1.0</value>
            <value>2.0</value>
            <value>3.0</value>
        </pivot>
    </classifications>-->

</statisticConfiguration>
', 11);
INSERT INTO gs_stored_data values(2, '<?xml version="1.0" encoding="UTF-8" standalone="yes"?>
<statisticConfiguration>
    <name>area_admin_classified</name>
    <title>Forest change in admin areas</title>
    <description>Compute the area for the administrative areas.</description>

    <topic>rdc</topic>
    <topic>area</topic>
    <topic>administrative</topic>
    <topic>forest</topic>

    <stats>
        <stat>SUM</stat>
    </stats>

    <deferredMode>true</deferredMode>

    <dataLayer>
        <file>/home/geosol/data/unredd/rdc_admin_area.tif</file>
    </dataLayer>

    <classificationLayer zonal="true">
        <file>/home/geosol/data/unredd/rdc_limite_administrative_tiled.tif</file>
        <nodata>65535</nodata>
    </classificationLayer>

    <classificationLayer>
        <file>/home/geosol/data/unredd/forest_classification_tiled.tif</file>
        <nodata>0.0</nodata>
        <pivot>
            <value>1.0</value>
            <value>2.0</value>
            <value>3.0</value>
            <value>4.0</value>
            <value>5.0</value>
            <value>6.0</value>
        </pivot>
    </classificationLayer>


    <output>
        <format>CSV</format>
        <separator>;</separator>
        <missingValue>0</missingValue>
        <NaNValue>-1</NaNValue>
        <file>area_admin_classified_6.csv</file>
    </output>

</statisticConfiguration>
', 12);


--init of the gs_security

INSERT INTO gs_security values(1, TRUE, TRUE, null, null, 1,1);
INSERT INTO gs_security values(2, TRUE, TRUE, null, null, 2,1);
INSERT INTO gs_security values(3, TRUE, TRUE, null, null, 3,1);
INSERT INTO gs_security values(4, TRUE, TRUE, null, null, 4,1);
INSERT INTO gs_security values(5, TRUE, TRUE, null, null, 5,1);
INSERT INTO gs_security values(6, TRUE, TRUE, null, null, 6,1);
INSERT INTO gs_security values(7, TRUE, TRUE, null, null, 7,1);
INSERT INTO gs_security values(8, TRUE, TRUE, null, null, 8,1);
INSERT INTO gs_security values(9, TRUE, TRUE, null, null, 9,1);
INSERT INTO gs_security values(10, TRUE, TRUE, null, null, 10,1);
INSERT INTO gs_security values(11, TRUE, TRUE, null, null, 11,1);
INSERT INTO gs_security values(12, TRUE, TRUE, null, null, 12,1);

