%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
%%%%%%%%%%%% GET THE BMS STRUCTURE %%%%%%%%%%%%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
function bms_struct=get_bms(fid,lenbms)
bms=fread(fid,lenbms);
bms_struct.len=lenbms;
bms_struct.NUnused_Bits=bms(4);
bms_struct.oct5=bms(5);
bms_struct.oct6=bms(6);
bms_struct.bitmap=uint8(bms(7:lenbms));
bms_struct.bmsvals=bms;
