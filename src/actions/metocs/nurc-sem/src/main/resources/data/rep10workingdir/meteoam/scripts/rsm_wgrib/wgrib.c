#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <stddef.h>
#include <math.h>
#include <float.h>
/* 
 * version 1.2.1 of grib headers  w. ebisuzaki 
 *         1.2.2 added access to spectral reference value l. kornblueh
 */

#ifndef INT2
#define INT2(a,b)   ((1-(int) ((unsigned) (a & 0x80) >> 6)) * (int) (((a & 0x7f) << 8) + b))
#endif

#define BDS_LEN(bds)		((int) ((bds[0]<<16)+(bds[1]<<8)+bds[2]))
#define BDS_Flag(bds)		(bds[3])

#define BDS_Grid(bds)		((bds[3] & 128) == 0)
#define BDS_Harmonic(bds)	(bds[3] & 128)

#define BDS_Packing(bds)	((bds[3] & 64) != 0)
#define BDS_SimplePacking(bds)	((bds[3] & 64) == 0)
#define BDS_ComplexPacking(bds)	((bds[3] & 64) != 0)

#define BDS_OriginalType(bds)	((bds[3] & 32) != 0)
#define BDS_OriginalFloat(bds)	((bds[3] & 32) == 0)
#define BDS_OriginalInt(bds)	((bds[3] & 32) != 0)

#define BDS_MoreFlags(bds)      ((bds[3] & 16) != 0)
#define BDS_UnusedBits(bds)	((int) (bds[3] & 15))

#define BDS_BinScale(bds)	INT2(bds[4],bds[5])

#define BDS_RefValue(bds)	(ibm2flt(bds+6))
#define BDS_NumBits(bds)	((int) bds[10])

#define BDS_Harmonic_RefValue(bds) (ibm2flt(bds+11))

#define BDS_DataStart(bds)      ((int) (11 + BDS_MoreFlags(bds)*3))

/* breaks if BDS_NumBits(bds) == 0 */
#define BDS_NValues(bds)        (((BDS_LEN(bds) - BDS_DataStart(bds))*8 - \
				BDS_UnusedBits(bds)) / BDS_NumBits(bds))

/*
#define BDS_NValues(bds)        ((BDS_NumBits(bds) == 0) ? 0 : \
				(((BDS_LEN(bds) - BDS_DataStart(bds))*8 - \
				BDS_UnusedBits(bds)) / BDS_NumBits(bds)))
*/


/* undefined value -- if bitmap */
#define UNDEFINED		9.999e20

/* version 1.2 of grib headers  w. ebisuzaki */

#define BMS_LEN(bms)		((bms) == NULL ? 0 : (bms[0]<<16)+(bms[1]<<8)+bms[2])
#define BMS_UnusedBits(bms)	((bms) == NULL ? 0 : bms[3])
#define BMS_StdMap(bms)		((bms) == NULL ? 0 : ((bms[4]<<8) + bms[5]))
#define BMS_bitmap(bms)		((bms) == NULL ? NULL : (bms)+6)
#define BMS_nxny(bms)		((((bms) == NULL) || BMS_StdMap(bms)) \
	? 0 : (BMS_LEN(bms)*8 - 48 - BMS_UnusedBits(bms)))
/* cnames_file.c */

/* search order for parameter names
 *
 * #define P_TABLE_FIRST
 * look at external parameter table first
 *
 * otherwise use builtin NCEP-2 or ECMWF-160 first
 */
/* #define P_TABLE_FIRST */

/* search order for external parameter table
 * 1) environment variable GRIBTAB
 * 2) environment variable gribtab
 * 3) the file 'gribtab' in current directory
 */


/* cnames.c */
/* then default values */
char *k5toa(unsigned char *pds);
char *k5_comments(unsigned char *pds);
int setup_user_table(int center, int subcenter, int ptable);


struct ParmTable {
	char *name, *comment;
};

/* version 1.4.3 of grib headers  w. ebisuzaki */
/* this version is incomplete */
/* 5/00 - dx/dy or di/dj controlled by bit 1 of resolution byte */
/* 8/00 - dx/dy or di/dj for polar and lambert not controlled by res. byte */
/* Added headers for the triangular grid of the gme model of DWD
         Helmut P. Frank, 13.09.2001 */
/* Clean up of triangular grid properties access and added spectral information
         Luis Kornblueh, 27.03.2002 */

#ifndef INT3
#define INT3(a,b,c) ((1-(int) ((unsigned) (a & 0x80) >> 6)) * (int) (((a & 127) << 16)+(b<<8)+c))
#endif
#ifndef INT2
#define INT2(a,b)   ((1-(int) ((unsigned) (a & 0x80) >> 6)) * (int) (((a & 127) << 8) + b))
#endif

#ifndef UINT4
#define UINT4(a,b,c,d) ((int) ((a << 24) + (b << 16) + (c << 8) + (d)))
#endif

#ifndef UINT3
#define UINT3(a,b,c) ((int) ((a << 16) + (b << 8) + (c)))
#endif

#ifndef UINT2
#define UINT2(a,b) ((int) ((a << 8) + (b)))
#endif


#define GDS_Len1(gds)		(gds[0])
#define GDS_Len2(gds)		(gds[1])
#define GDS_Len3(gds)		(gds[2])
#define GDS_LEN(gds)		((int) ((gds[0]<<16)+(gds[1]<<8)+gds[2]))

#define GDS_NV(gds)		(gds[3])
#define GDS_DataType(gds)	(gds[5])

#define GDS_LatLon(gds)		(gds[5] == 0)
#define GDS_Mercator(gds)	(gds[5] == 1)
#define GDS_Gnomonic(gds)	(gds[5] == 2)
#define GDS_Lambert(gds)	(gds[5] == 3)
#define GDS_Gaussian(gds)	(gds[5] == 4)
#define GDS_Polar(gds)		(gds[5] == 5)
#define GDS_RotLL(gds)		(gds[5] == 10)
#define GDS_Harmonic(gds)	(gds[5] == 50)
#define GDS_Triangular(gds)	(gds[5] == 192)
#define GDS_ssEgrid(gds)	(gds[5] == 201)	/* semi-staggered E grid */
#define GDS_fEgrid(gds)		(gds[5] == 202) /* filled E grid */
#define GDS_ss2dEgrid(gds)	(gds[5] == 203) /* semi-staggered E grid 2 d*/

#define GDS_has_dy(mode)	((mode) & 128)
#define GDS_LatLon_nx(gds)	((int) ((gds[6] << 8) + gds[7]))
#define GDS_LatLon_ny(gds)	((int) ((gds[8] << 8) + gds[9]))
#define GDS_LatLon_La1(gds)	INT3(gds[10],gds[11],gds[12])
#define GDS_LatLon_Lo1(gds)	INT3(gds[13],gds[14],gds[15])
#define GDS_LatLon_mode(gds)	(gds[16])
#define GDS_LatLon_La2(gds)	INT3(gds[17],gds[18],gds[19])
#define GDS_LatLon_Lo2(gds)	INT3(gds[20],gds[21],gds[22])

#define GDS_LatLon_dx(gds)      (gds[16] & 128 ? INT2(gds[23],gds[24]) : 0)
#define GDS_LatLon_dy(gds)      (gds[16] & 128 ? INT2(gds[25],gds[26]) : 0)
#define GDS_Gaussian_nlat(gds)  ((gds[25]<<8)+gds[26])

#define GDS_LatLon_scan(gds)	(gds[27])

#define GDS_Polar_nx(gds)	((gds[6] << 8) + gds[7])
#define GDS_Polar_ny(gds)	((gds[8] << 8) + gds[9])
#define GDS_Polar_La1(gds)	INT3(gds[10],gds[11],gds[12])
#define GDS_Polar_Lo1(gds)	INT3(gds[13],gds[14],gds[15])
#define GDS_Polar_mode(gds)	(gds[16])
#define GDS_Polar_Lov(gds)	INT3(gds[17],gds[18],gds[19])
#define GDS_Polar_scan(gds)	(gds[27])
#define GDS_Polar_Dx(gds)	INT3(gds[20], gds[21], gds[22])
#define GDS_Polar_Dy(gds)	INT3(gds[23], gds[24], gds[25])
#define GDS_Polar_pole(gds)	((gds[26] & 128) == 128)

#define GDS_Lambert_nx(gds)	((gds[6] << 8) + gds[7])
#define GDS_Lambert_ny(gds)	((gds[8] << 8) + gds[9])
#define GDS_Lambert_La1(gds)	INT3(gds[10],gds[11],gds[12])
#define GDS_Lambert_Lo1(gds)	INT3(gds[13],gds[14],gds[15])
#define GDS_Lambert_mode(gds)	(gds[16])
#define GDS_Lambert_Lov(gds)	INT3(gds[17],gds[18],gds[19])
#define GDS_Lambert_dx(gds)	INT3(gds[20],gds[21],gds[22])
#define GDS_Lambert_dy(gds)	INT3(gds[23],gds[24],gds[25])
#define GDS_Lambert_NP(gds)	((gds[26] & 128) == 0)
#define GDS_Lambert_scan(gds)   (gds[27])
#define GDS_Lambert_Latin1(gds)	INT3(gds[28],gds[29],gds[30])
#define GDS_Lambert_Latin2(gds)	INT3(gds[31],gds[32],gds[33])
#define GDS_Lambert_LatSP(gds)	INT3(gds[34],gds[35],gds[36])
#define GDS_Lambert_LonSP(gds)	INT3(gds[37],gds[37],gds[37])

#define GDS_ssEgrid_n(gds)	UINT2(gds[6],gds[7])
#define GDS_ssEgrid_n_dum(gds)  UINT2(gds[8],gds[9])
#define GDS_ssEgrid_La1(gds)	INT3(gds[10],gds[11],gds[12])
#define GDS_ssEgrid_Lo1(gds)	INT3(gds[13],gds[14],gds[15])
#define GDS_ssEgrid_mode(gds)	(gds[16])
#define GDS_ssEgrid_La2(gds)	UINT3(gds[17],gds[18],gds[19])
#define GDS_ssEgrid_Lo2(gds)	UINT3(gds[20],gds[21],gds[22])
#define GDS_ssEgrid_di(gds)	(gds[16] & 128 ? INT2(gds[23],gds[24]) : 0)
#define GDS_ssEgrid_dj(gds)	(gds[16] & 128 ? INT2(gds[25],gds[26]) : 0)
#define GDS_ssEgrid_scan(gds)	(gds[27])

#define GDS_fEgrid_n(gds)	UINT2(gds[6],gds[7])
#define GDS_fEgrid_n_dum(gds)   UINT2(gds[8],gds[9])
#define GDS_fEgrid_La1(gds)	INT3(gds[10],gds[11],gds[12])
#define GDS_fEgrid_Lo1(gds)	INT3(gds[13],gds[14],gds[15])
#define GDS_fEgrid_mode(gds)	(gds[16])
#define GDS_fEgrid_La2(gds)	UINT3(gds[17],gds[18],gds[19])
#define GDS_fEgrid_Lo2(gds)	UINT3(gds[20],gds[21],gds[22])
#define GDS_fEgrid_di(gds)	(gds[16] & 128 ? INT2(gds[23],gds[24]) : 0)
#define GDS_fEgrid_dj(gds)	(gds[16] & 128 ? INT2(gds[25],gds[26]) : 0)
#define GDS_fEgrid_scan(gds)	(gds[27])

#define GDS_ss2dEgrid_nx(gds)     UINT2(gds[6],gds[7])
#define GDS_ss2dEgrid_ny(gds)     UINT2(gds[8],gds[9])
#define GDS_ss2dEgrid_La1(gds)    INT3(gds[10],gds[11],gds[12])
#define GDS_ss2dEgrid_Lo1(gds)    INT3(gds[13],gds[14],gds[15])
#define GDS_ss2dEgrid_mode(gds)   (gds[16])
#define GDS_ss2dEgrid_La2(gds)    INT3(gds[17],gds[18],gds[19])
#define GDS_ss2dEgrid_Lo2(gds)    INT3(gds[20],gds[21],gds[22])
#define GDS_ss2dEgrid_di(gds)     (gds[16] & 128 ? INT2(gds[23],gds[24]) : 0)
#define GDS_ss2dEgrid_dj(gds)     (gds[16] & 128 ? INT2(gds[25],gds[26]) : 0)
#define GDS_ss2dEgrid_scan(gds)   (gds[27])


#define GDS_Merc_nx(gds)	UINT2(gds[6],gds[7])
#define GDS_Merc_ny(gds)	UINT2(gds[8],gds[9])
#define GDS_Merc_La1(gds)	INT3(gds[10],gds[11],gds[12])
#define GDS_Merc_Lo1(gds)	INT3(gds[13],gds[14],gds[15])
#define GDS_Merc_mode(gds)	(gds[16])
#define GDS_Merc_La2(gds)	INT3(gds[17],gds[18],gds[19])
#define GDS_Merc_Lo2(gds)	INT3(gds[20],gds[21],gds[22])
#define GDS_Merc_Latin(gds)	INT3(gds[23],gds[24],gds[25])
#define GDS_Merc_scan(gds)	(gds[27])
#define GDS_Merc_dx(gds)        (gds[16] & 128 ? INT3(gds[28],gds[29],gds[30]) : 0)
#define GDS_Merc_dy(gds)        (gds[16] & 128 ? INT3(gds[31],gds[32],gds[33]) : 0)

/* rotated Lat-lon grid */

#define GDS_RotLL_nx(gds)	UINT2(gds[6],gds[7])
#define GDS_RotLL_ny(gds)	UINT2(gds[8],gds[9])
#define GDS_RotLL_La1(gds)	INT3(gds[10],gds[11],gds[12])
#define GDS_RotLL_Lo1(gds)	INT3(gds[13],gds[14],gds[15])
#define GDS_RotLL_mode(gds)	(gds[16])
#define GDS_RotLL_La2(gds)	INT3(gds[17],gds[18],gds[19])
#define GDS_RotLL_Lo2(gds)	INT3(gds[20],gds[21],gds[22])
#define GDS_RotLL_dx(gds)       (gds[16] & 128 ? INT2(gds[23],gds[24]) : 0)
#define GDS_RotLL_dy(gds)       (gds[16] & 128 ? INT2(gds[25],gds[26]) : 0)
#define GDS_RotLL_scan(gds)	(gds[27])
#define GDS_RotLL_LaSP(gds)	INT3(gds[32],gds[33],gds[34])
#define GDS_RotLL_LoSP(gds)	INT3(gds[35],gds[36],gds[37])
#define GDS_RotLL_RotAng(gds)	ibm2flt(&(gds[38]))

/* Triangular grid of DWD */
#define GDS_Triangular_ni2(gds)	INT2(gds[6],gds[7])
#define GDS_Triangular_ni3(gds)	INT2(gds[8],gds[9])
#define GDS_Triangular_ni(gds)	INT3(gds[13],gds[14],gds[15])
#define GDS_Triangular_nd(gds)  INT3(gds[10],gds[11],gds[12])

/* Harmonics data */
#define GDS_Harmonic_nj(gds)     ((int) ((gds[6] << 8) + gds[7])) 
#define GDS_Harmonic_nk(gds)     ((int) ((gds[8] << 8) + gds[9])) 
#define GDS_Harmonic_nm(gds)     ((int) ((gds[10] << 8) + gds[11])) 
#define GDS_Harmonic_type(gds)   (gds[12])
#define GDS_Harmonic_mode(gds)   (gds[13])

/* index of NV and PV */
#define GDS_PV(gds)		((gds[3] == 0) ? -1 : (int) gds[4] - 1)
#define GDS_PL(gds)		((gds[4] == 255) ? -1 : (int) gds[3] * 4 + (int) gds[4] - 1)

enum Def_NCEP_Table {rean, opn, rean_nowarn, opn_nowarn};

unsigned char *seek_grib(FILE *file, long *pos, long *len_grib, 
        unsigned char *buffer, unsigned int buf_len);

int read_grib(FILE *file, long pos, long len_grib, unsigned char *buffer);

double ibm2flt(unsigned char *ibm);
 
void BDS_unpack(float *flt, unsigned char *bds, unsigned char *bitmap,
        int n_bits, int n, double ref, double scale);

double int_power(double x, int y);

int flt2ieee(float x, unsigned char *ieee);

int wrtieee(float *array, int n, int header, FILE *output);
int wrtieee_header(unsigned int n, FILE *output);

void levels(int, int, int);
 
void PDStimes(int time_range, int p1, int p2, int time_unit);

int missing_points(unsigned char *bitmap, int n);

void EC_ext(unsigned char *pds, char *prefix, char *suffix, int verbose);

int GDS_grid(unsigned char *gds, unsigned char *bds, int *nx, int *ny, 
             long int *nxny);

void GDS_prt_thin_lon(unsigned char *gds);

void GDS_winds(unsigned char *gds, int verbose);

int PDS_date(unsigned char *pds, int option, int verf_time);

int add_time(int *year, int *month, int *day, int *hour, int dtime, int unit);

int verf_time(unsigned char *pds, int *year, int *month, int *day, int *hour);

void print_pds(unsigned char *pds, int print_PDS, int print_PDS10, int verbose);
void print_gds(unsigned char *gds, int print_GDS, int print_GDS10, int verbose);

void ensemble(unsigned char *pds, int mode);
/* version 3.4 of grib headers  w. ebisuzaki */
/* this version is incomplete */
/* add center DWD    Helmut P. Frank */
/* 10/02 add center CPTEC */

#ifndef INT2
#define INT2(a,b)   ((1-(int) ((unsigned) (a & 0x80) >> 6)) * (int) (((a & 0x7f) << 8) + b))
#endif
#ifndef UINT4
#define UINT4(a,b,c,d) ((int) ((a << 24) + (b << 16) + (c << 8) + (d)))
#endif
#ifndef UINT2
#define UINT2(a,b) ((int) ((a << 8) + (b)))
#endif

#define PDS_Len1(pds)		(pds[0])
#define PDS_Len2(pds)		(pds[1])
#define PDS_Len3(pds)		(pds[2])
#define PDS_LEN(pds)		((int) ((pds[0]<<16)+(pds[1]<<8)+pds[2]))
#define PDS_Vsn(pds)		(pds[3])
#define PDS_Center(pds)		(pds[4])
#define PDS_Model(pds)		(pds[5])
#define PDS_Grid(pds)		(pds[6])
#define PDS_HAS_GDS(pds)	((pds[7] & 128) != 0)
#define PDS_HAS_BMS(pds)	((pds[7] & 64) != 0)
#define PDS_PARAM(pds)		(pds[8])
#define PDS_L_TYPE(pds)		(pds[9])
#define PDS_LEVEL1(pds)		(pds[10])
#define PDS_LEVEL2(pds)		(pds[11])

#define PDS_KPDS5(pds)		(pds[8])
#define PDS_KPDS6(pds)		(pds[9])
#define PDS_KPDS7(pds)		((int) ((pds[10]<<8) + pds[11]))

/* this requires a 32-bit default integer machine */
#define PDS_Field(pds)		((pds[8]<<24)+(pds[9]<<16)+(pds[10]<<8)+pds[11])

#define PDS_Year(pds)		(pds[12])
#define PDS_Month(pds)		(pds[13])
#define PDS_Day(pds)		(pds[14])
#define PDS_Hour(pds)		(pds[15])
#define PDS_Minute(pds)		(pds[16])
#define PDS_ForecastTimeUnit(pds)	(pds[17])
#define PDS_P1(pds)		(pds[18])
#define PDS_P2(pds)		(pds[19])
#define PDS_TimeRange(pds)	(pds[20])
#define PDS_NumAve(pds)		((int) ((pds[21]<<8)+pds[22]))
#define PDS_NumMissing(pds)	(pds[23])
#define PDS_Century(pds)	(pds[24])
#define PDS_Subcenter(pds)	(pds[25])
#define PDS_DecimalScale(pds)	INT2(pds[26],pds[27])
/* old #define PDS_Year4(pds)   (pds[12] + 100*(pds[24] - (pds[12] != 0))) */
#define PDS_Year4(pds)          (pds[12] + 100*(pds[24] - 1))

/* various centers */
#define NMC			7
#define ECMWF			98
#define DWD			78
#define CMC			54
#define CPTEC			46

/* ECMWF Extensions */

#define PDS_EcLocalId(pds)	(PDS_LEN(pds) >= 41 ? (pds[40]) : 0)
#define PDS_EcClass(pds)	(PDS_LEN(pds) >= 42 ? (pds[41]) : 0)
#define PDS_EcType(pds)		(PDS_LEN(pds) >= 43 ? (pds[42]) : 0)
#define PDS_EcStream(pds)	(PDS_LEN(pds) >= 45 ? (INT2(pds[43], pds[44])) : 0)

#define PDS_EcENS(pds)		(PDS_LEN(pds) >= 52 && pds[40] == 1 && \
				pds[43] * 256 + pds[44] == 1035 && pds[50] != 0)
#define PDS_EcFcstNo(pds)	(pds[50])
#define PDS_EcNoFcst(pds)	(pds[51])

#define PDS_Ec16Version(pds)	(pds + 45)
#define PDS_Ec16Number(pds)	(PDS_EcLocalId(pds) == 16 ? UINT2(pds[49],pds[50]) : 0)
#define PDS_Ec16SysNum(pds)	(PDS_EcLocalId(pds) == 16 ? UINT2(pds[51],pds[52]) : 0)
#define PDS_Ec16MethodNum(pds)	(PDS_EcLocalId(pds) == 16 ? UINT2(pds[53],pds[54]) : 0)
#define PDS_Ec16VerfMon(pds)	(PDS_EcLocalId(pds) == 16 ? UINT4(pds[55],pds[56],pds[57],pds[58]) : 0)
#define PDS_Ec16AvePeriod(pds)	(PDS_EcLocalId(pds) == 16 ? pds[59] : 0)
#define PDS_Ec16FcstMon(pds)	(PDS_EcLocalId(pds) == 16 ? UINT2(pds[60],pds[61]) : 0)

/* NCEP Extensions */

#define PDS_NcepENS(pds)	(PDS_LEN(pds) >= 44 && pds[25] == 2 && pds[40] == 1)
#define PDS_NcepFcstType(pds)	(pds[41])
#define PDS_NcepFcstNo(pds)	(pds[42])
#define PDS_NcepFcstProd(pds)	(pds[43])

/* time units */

#define MINUTE  0
#define HOUR    1
#define DAY     2
#define MONTH   3
#define YEAR    4
#define DECADE  5
#define NORMAL  6
#define CENTURY 7
#define HOURS3  10
#define HOURS6  11
#define HOURS12  12
#define SECOND  254



#define VERSION "v1.8.0.10 (12-01-05) Wesley Ebisuzaki\n\t\tDWD-tables 2,201-203 (8-19-2003) Helmut P. Frank\n\t\tspectral: Luis Kornblueh (MPI)"

#define CHECK_GRIB

/*
 * wgrib.c is placed into the public domain.  While you could
 * legally do anything you want with the code, telling the world
 * that you wrote it would be uncool.  Selling it would be really
 * uncool.  The code was originally written for NMC/NCAR Reanalysis 
 * and handles most GRIB files except for the ECMWF spectral files.
 * (ECMWF's spectral->grid code are copyrighted and in FORTRAN.)
 * The code, as usual, is not waranteed to be fit for any purpose 
 * what so ever.  However, wgrib is operational NCEP code, so it
 * better work for our files.
 */

/*
 * wgrib.c extract/inventory grib records
 *
 *                              Wesley Ebisuzaki
 *
 * See Changes for update information
 *
 */

/*
 * MSEEK = I/O buffer size for seek_grib
 */

#define MSEEK 1024
#define BUFF_ALLOC0	40000


#ifndef min
#define min(a,b)  ((a) < (b) ? (a) : (b))
#define max(a,b)  ((a) < (b) ? (b) : (a))
#endif

#ifndef DEF_T62_NCEP_TABLE
#define DEF_T62_NCEP_TABLE	rean
#endif
enum Def_NCEP_Table def_ncep_table = DEF_T62_NCEP_TABLE;
int minute = 0;
int ncep_ens = 0;

int main(int argc, char **argv) {

    unsigned char *buffer;
    float *array;
    double temp, rmin, rmax;
    int i, nx, ny, file_arg;
    long int len_grib, pos = 0, nxny, buffer_size, n_dump, count = 1;
    unsigned char *msg, *pds, *gds, *bms, *bds, *pointer;
    FILE *input, *dump_file = NULL;
    char line[200];
    enum {BINARY, TEXT, IEEE, GRIB, NONE} output_type = NONE;
    enum {DUMP_ALL, DUMP_RECORD, DUMP_POSITION, DUMP_LIST, INVENTORY} 
	mode = INVENTORY;
    enum {none, dwd, simple} header = simple;

    long int dump = -1;
    int verbose = 0, append = 0, v_time = 0, year_4 = 0, output_PDS_GDS = 0;
    int print_GDS = 0, print_GDS10 = 0, print_PDS = 0, print_PDS10 = 0;
    char *dump_file_name = "dump", open_parm[3];
    int return_code = 0;

    if (argc == 1) {
	fprintf(stderr, "\nPortable Grib decoder for %s etc.\n",
	    (def_ncep_table == opn_nowarn || def_ncep_table == opn) ?
	    "NCEP Operations" : "NCEP/NCAR Reanalysis");
	fprintf(stderr, "   it slices, dices    %s\n", VERSION);
	fprintf(stderr, "   usage: %s [grib file] [options]\n\n", argv[0]);

	fprintf(stderr, "Inventory/diagnostic-output selections\n");
	fprintf(stderr, "   -s/-v                   short/verbose inventory\n");
	fprintf(stderr, "   -V                      diagnostic output (not inventory)\n");
	fprintf(stderr, "   (none)                  regular inventory\n");

	fprintf(stderr, " Options\n");
	fprintf(stderr, "   -PDS/-PDS10             print PDS in hex/decimal\n");
	fprintf(stderr, "   -GDS/-GDS10             print GDS in hex/decimal\n");
	fprintf(stderr, "   -verf                   print forecast verification time\n");
	fprintf(stderr, "   -ncep_opn/-ncep_rean    default T62 NCEP grib table\n");
	fprintf(stderr, "   -4yr                    print year using 4 digits\n");
	fprintf(stderr, "   -min                    print minutes\n");
	fprintf(stderr, "   -ncep_ens               ensemble info encoded in ncep format\n");

	fprintf(stderr, "Decoding GRIB selection\n");
	fprintf(stderr, "   -d [record number|all]  decode record number\n");
	fprintf(stderr, "   -p [byte position]      decode record at byte position\n");
	fprintf(stderr, "   -i                      decode controlled by stdin (inventory list)\n");
	fprintf(stderr, "   (none)                  no decoding\n");

	fprintf(stderr, " Options\n");
	fprintf(stderr, "   -text/-ieee/-grib/-bin  convert to text/ieee/grib/bin (default)\n");
	fprintf(stderr, "   -nh/-h                  output will have no headers/headers (default)\n");
	fprintf(stderr, "   -dwdgrib                output dwd headers, grib (do not append)\n");
	fprintf(stderr, "   -H                      output will include PDS and GDS (-bin/-ieee only)\n");
	fprintf(stderr, "   -append                 append to output file\n");
	fprintf(stderr, "   -o [file]               output file name, 'dump' is default\n");
	exit(8);
    }
    file_arg = 0;
    for (i = 1; i < argc; i++) {
	if (strcmp(argv[i],"-PDS") == 0) {
	    print_PDS = 1;
	    continue;
	}
	if (strcmp(argv[i],"-PDS10") == 0) {
	    print_PDS10 = 1;
	    continue;
	}
	if (strcmp(argv[i],"-GDS") == 0) {
	    print_GDS = 1;
	    continue;
	}
	if (strcmp(argv[i],"-GDS10") == 0) {
	    print_GDS10 = 1;
	    continue;
	}
	if (strcmp(argv[i],"-v") == 0) {
	    verbose = 1;
	    continue;
	}
	if (strcmp(argv[i],"-V") == 0) {
	    verbose = 2;
	    continue;
	}
	if (strcmp(argv[i],"-s") == 0) {
	    verbose = -1;
	    continue;
	}
	if (strcmp(argv[i],"-text") == 0) {
	    output_type = TEXT;
	    continue;
	}
	if (strcmp(argv[i],"-bin") == 0) {
	    output_type = BINARY;
	    continue;
	}
	if (strcmp(argv[i],"-ieee") == 0) {
	    output_type = IEEE;
	    continue;
	}
	if (strcmp(argv[i],"-grib") == 0) {
	    output_type = GRIB;
	    continue;
	}
	if (strcmp(argv[i],"-nh") == 0) {
	    header = none;
	    continue;
	}
	if (strcmp(argv[i],"-h") == 0) {
	    header = simple;
	    continue;
	}
	if (strcmp(argv[i],"-dwdgrib") == 0) {
	    header = dwd;
	    output_type = GRIB;
	    continue;
	}
	if (strcmp(argv[i],"-append") == 0) {
	    append = 1;
	    continue;
	}
	if (strcmp(argv[i],"-verf") == 0) {
	    v_time = 1;
	    continue;
        }
	if (strcmp(argv[i],"-d") == 0) {
	    if (strcmp(argv[i+1],"all") == 0) {
	        mode = DUMP_ALL;
	    }
	    else {
	        dump = atol(argv[i+1]);
	        mode = DUMP_RECORD;
	    }
	    i++;
	    if (output_type == NONE) output_type = BINARY;
	    continue;
	}
	if (strcmp(argv[i],"-p") == 0) {
	    pos = atol(argv[i+1]);
	    i++;
	    dump = 1;
	    if (output_type == NONE) output_type = BINARY;
	    mode = DUMP_POSITION;
	    continue;
	}
	if (strcmp(argv[i],"-i") == 0) {
	    if (output_type == NONE) output_type = BINARY;
	    mode = DUMP_LIST;
	    continue;
	}
	if (strcmp(argv[i],"-H") == 0) {
	    output_PDS_GDS = 1;
	    continue;
	}
	if (strcmp(argv[i],"-NH") == 0) {
	    output_PDS_GDS = 0;
	    continue;
	}
	if (strcmp(argv[i],"-4yr") == 0) {
	    year_4 = 1;
	    continue;
	}
	if (strcmp(argv[i],"-ncep_opn") == 0) {
	    def_ncep_table = opn_nowarn;
	    continue;
	}
	if (strcmp(argv[i],"-ncep_rean") == 0) {
	    def_ncep_table = rean_nowarn;
	    continue;
	}
	if (strcmp(argv[i],"-o") == 0) {
	    dump_file_name = argv[i+1];
	    i++;
	    continue;
	}
	if (strcmp(argv[i],"--v") == 0) {
	    printf("wgrib: %s\n", VERSION);
	    exit(0);
	}
	if (strcmp(argv[i],"-min") == 0) {
	    minute = 1;
	    continue;
	}
	if (strcmp(argv[i],"-ncep_ens") == 0) {
	    ncep_ens = 1;
	    continue;
	}
	if (file_arg == 0) {
	    file_arg = i;
	}
	else {
	    fprintf(stderr,"argument: %s ????\n", argv[i]);
	}
    }
    if (file_arg == 0) {
	fprintf(stderr,"no GRIB file to process\n");
	exit(8);
    }
    if ((input = fopen(argv[file_arg],"rb")) == NULL) {
        fprintf(stderr,"could not open file: %s\n", argv[file_arg]);
        exit(7);
    }

    if ((buffer = (unsigned char *) malloc(BUFF_ALLOC0)) == NULL) {
	fprintf(stderr,"not enough memory\n");
    }
    buffer_size = BUFF_ALLOC0;

    /* open output file */
    if (mode != INVENTORY) {
	open_parm[0] = append ? 'a' : 'w'; open_parm[1] = 'b'; open_parm[2] = '\0';
	if (output_type == TEXT) open_parm[1] = '\0';

	if ((dump_file = fopen(dump_file_name,open_parm)) == NULL) {
	    fprintf(stderr,"could not open dump file\n");
	    exit(8);
        }
	if (header == dwd && output_type == GRIB) wrtieee_header(0, dump_file);
    }

    /* skip dump - 1 records */
    for (i = 1; i < dump; i++) {
	msg = seek_grib(input, &pos, &len_grib, buffer, MSEEK);
	if (msg == NULL) {
	    fprintf(stderr, "ran out of data or bad file\n");
	    exit(8);
	}
	pos += len_grib;
    }
    if (dump > 0) count += dump - 1;
    n_dump = 0;

    for (;;) {
	if (n_dump == 1 && (mode == DUMP_RECORD || mode == DUMP_POSITION)) break;
	if (mode == DUMP_LIST) {
	    if (fgets(line,sizeof(line), stdin) == NULL) break;
            line[sizeof(line) - 1] = 0;
            if (sscanf(line,"%ld:%ld:", &count, &pos) != 2) {
		fprintf(stderr,"bad input from stdin\n");
                fprintf(stderr,"   %s\n", line);
	        exit(8);
	    }
	}

	msg = seek_grib(input, &pos, &len_grib, buffer, MSEEK);
	if (msg == NULL) {
	    if (mode == INVENTORY || mode == DUMP_ALL) break;
	    fprintf(stderr,"missing GRIB record(s)\n");
	    exit(8);
	}

        /* read all whole grib record */
        if (len_grib + msg - buffer > buffer_size) {
            buffer_size = len_grib + msg - buffer + 1000;
            buffer = (unsigned char *) realloc((void *) buffer, buffer_size);
            if (buffer == NULL) {
                fprintf(stderr,"ran out of memory\n");
                exit(8);
            }
        }
        read_grib(input, pos, len_grib, buffer);

	/* parse grib message */

	msg = buffer;
        pds = (msg + 8);
        pointer = pds + PDS_LEN(pds);
#ifdef DEBUG
	printf("LEN_GRIB= 0x%x\n", len_grib);
	printf("PDS_LEN= 0x%x: at 0x%x\n", PDS_LEN(pds),pds-msg);
#endif
        if (PDS_HAS_GDS(pds)) {
            gds = pointer;
            pointer += GDS_LEN(gds);
#ifdef DEBUG
	    printf("GDS_LEN= 0x%x: at 0x%x\n", GDS_LEN(gds), gds-msg);
#endif
        }
        else {
            gds = NULL;
        }

        if (PDS_HAS_BMS(pds)) {
            bms = pointer;
            pointer += BMS_LEN(bms);
#ifdef DEBUG
	    printf("BMS_LEN= 0x%x: at 0x%x\n", BMS_LEN(bms),bms-msg);
#endif
        }
        else {
            bms = NULL;
        }

        bds = pointer;
        pointer += BDS_LEN(bds);
#ifdef DEBUG
	printf("BDS_LEN= 0x%x: at 0x%x\n", BDS_LEN(bds),bds-msg);
#endif

#ifdef DEBUG
	printf("END_LEN= 0x%x: at 0x%x\n", 4,pointer-msg);
	if (pointer-msg+4 != len_grib) {
	    fprintf(stderr,"Len of grib message is inconsistent.\n");
	}
#endif

        /* end section - "7777" in ascii */
        if (pointer[0] != 0x37 || pointer[1] != 0x37 ||
            pointer[2] != 0x37 || pointer[3] != 0x37) {
            fprintf(stderr,"\n\n    missing end section\n");
            fprintf(stderr, "%2x %2x %2x %2x\n", pointer[0], pointer[1], 
		pointer[2], pointer[3]);
#ifdef DEBUG
	    printf("ignoring missing end section\n");
#else
	    exit(8);
#endif
        }

	/* figure out size of array */
	if (gds != NULL) {
	    GDS_grid(gds, bds, &nx, &ny, &nxny);
	}
	else if (bms != NULL) {
	    nxny = nx = BMS_nxny(bms);
	    ny = 1;
	}
	else {
	    if (BDS_NumBits(bds) == 0) {
                nxny = nx = 1;
                fprintf(stderr,"Missing GDS, constant record .. cannot "
                    "determine number of data points\n");
	    }
	    else {
	        nxny = nx = BDS_NValues(bds);
	    }
	    ny = 1;
	}

#ifdef CHECK_GRIB
	if (gds && ! GDS_Harmonic(gds)) {
	/* this grib check only works for simple packing */
	/* turn off if harmonic */
	    if (BDS_NumBits(bds) != 0) {
	        i = BDS_NValues(bds);
	        if (bms != NULL) {
	            i += missing_points(BMS_bitmap(bms),nxny);
	        }
	        if (i != nxny) {
	            fprintf(stderr,"grib header at record %ld: two values of nxny %ld %d\n",
			count,nxny,i);
		    fprintf(stderr,"   LEN %d DataStart %d UnusedBits %d #Bits %d nxny %ld\n",
			BDS_LEN(bds), BDS_DataStart(bds),BDS_UnusedBits(bds),
			BDS_NumBits(bds), nxny);
		    return_code = 15;
		    nxny = nx = i;
		    ny = 1;
	        }
	    }
 
        }
#endif
 
        if (verbose <= 0) {
	    printf("%ld:%ld:d=", count, pos);
	    PDS_date(pds,year_4,v_time);
	    printf(":%s:", k5toa(pds));

            if (verbose == 0) printf("kpds5=%d:kpds6=%d:kpds7=%d:TR=%d:P1=%d:P2=%d:TimeU=%d:",
	        PDS_PARAM(pds),PDS_KPDS6(pds),PDS_KPDS7(pds),
	        PDS_TimeRange(pds),PDS_P1(pds),PDS_P2(pds),
                PDS_ForecastTimeUnit(pds));
	    levels(PDS_KPDS6(pds), PDS_KPDS7(pds),PDS_Center(pds)); printf(":");
	    PDStimes(PDS_TimeRange(pds),PDS_P1(pds),PDS_P2(pds),
                PDS_ForecastTimeUnit(pds));
	    if (PDS_Center(pds) == ECMWF) EC_ext(pds,"",":",verbose);
	    ensemble(pds, verbose);
	    printf("NAve=%d",PDS_NumAve(pds));
	    if (print_PDS || print_PDS10) print_pds(pds, print_PDS, print_PDS10, verbose);
	    if (gds && (print_GDS || print_GDS10)) print_gds(gds, print_GDS, print_GDS10, verbose);
            printf("\n");
       }
       else if (verbose == 1) {
	    printf("%ld:%ld:D=", count, pos);
            PDS_date(pds, 1, v_time);
	    printf(":%s:", k5toa(pds));
	    levels(PDS_KPDS6(pds), PDS_KPDS7(pds), PDS_Center(pds)); printf(":");
            printf("kpds=%d,%d,%d:",
	        PDS_PARAM(pds),PDS_KPDS6(pds),PDS_KPDS7(pds));
	    PDStimes(PDS_TimeRange(pds),PDS_P1(pds),PDS_P2(pds),
                PDS_ForecastTimeUnit(pds));
	    if (PDS_Center(pds) == ECMWF) EC_ext(pds,"",":",verbose);
	    ensemble(pds, verbose);
	    GDS_winds(gds, verbose);
            printf("\"%s", k5_comments(pds));
	    if (print_PDS || print_PDS10) print_pds(pds, print_PDS, print_PDS10, verbose);
	    if (gds && (print_GDS || print_GDS10)) print_gds(gds, print_GDS, print_GDS10, verbose);
            printf("\n");
	}
        else if (verbose == 2) {
	    printf("rec %ld:%ld:date ", count, pos);
	    PDS_date(pds, 1, v_time);
	    printf(" %s kpds5=%d kpds6=%d kpds7=%d levels=(%d,%d) grid=%d ", 
	        k5toa(pds), PDS_PARAM(pds), PDS_KPDS6(pds), PDS_KPDS7(pds), 
                PDS_LEVEL1(pds), PDS_LEVEL2(pds), PDS_Grid(pds));
	        levels(PDS_KPDS6(pds),PDS_KPDS7(pds),PDS_Center(pds));

	    printf(" ");
	    if (PDS_Center(pds) == ECMWF) EC_ext(pds,""," ",verbose);
	    ensemble(pds, verbose);
	    PDStimes(PDS_TimeRange(pds),PDS_P1(pds),PDS_P2(pds),
                 PDS_ForecastTimeUnit(pds));
	    if (bms != NULL) 
		printf(" bitmap: %d undef", missing_points(BMS_bitmap(bms),nxny));
            printf("\n  %s=%s\n", k5toa(pds), k5_comments(pds));
	
            printf("  timerange %d P1 %d P2 %d TimeU %d  nx %d ny %d GDS grid %d "
		"num_in_ave %d missing %d\n", 
	        PDS_TimeRange(pds),PDS_P1(pds),PDS_P2(pds), 
                PDS_ForecastTimeUnit(pds), nx, ny, 
                gds == NULL ? -1 : GDS_DataType(gds), 
                PDS_NumAve(pds), PDS_NumMissing(pds));

	    printf("  center %d subcenter %d process %d Table %d", 
		PDS_Center(pds),PDS_Subcenter(pds),PDS_Model(pds),
                PDS_Vsn(pds));
	    GDS_winds(gds, verbose);
	    printf("\n");

	    if (gds && GDS_LatLon(gds) && nx != -1) 
		printf("  latlon: lat  %f to %f by %f  nxny %ld\n"
                       "          long %f to %f by %f, (%d x %d) scan %d "
                       "mode %d bdsgrid %d\n",
		  0.001*GDS_LatLon_La1(gds), 0.001*GDS_LatLon_La2(gds),
		  0.001*GDS_LatLon_dy(gds), nxny, 0.001*GDS_LatLon_Lo1(gds),
		  0.001*GDS_LatLon_Lo2(gds), 0.001*GDS_LatLon_dx(gds),
	    	  nx, ny, GDS_LatLon_scan(gds), GDS_LatLon_mode(gds),
		  BDS_Grid(bds));
	    else if (gds && GDS_LatLon(gds) && nx == -1) {
		printf("  thinned latlon: lat  %f to %f by %f  nxny %ld\n"
                       "          long %f to %f, %ld grid pts   (%d x %d) scan %d"
			" mode %d bdsgrid %d\n",
		  0.001*GDS_LatLon_La1(gds), 0.001*GDS_LatLon_La2(gds),
		  0.001*GDS_LatLon_dy(gds), nxny, 0.001*GDS_LatLon_Lo1(gds),
		  0.001*GDS_LatLon_Lo2(gds),
	    	  nxny, nx, ny, GDS_LatLon_scan(gds), GDS_LatLon_mode(gds),
		  BDS_Grid(bds));
		  GDS_prt_thin_lon(gds);
	    }
	    else if (gds && GDS_Gaussian(gds) && nx != -1)
		printf("  gaussian: lat  %f to %f\n"
                       "            long %f to %f by %f, (%d x %d) scan %d"
			" mode %d bdsgrid %d\n",
		  0.001*GDS_LatLon_La1(gds), 0.001*GDS_LatLon_La2(gds),
		  0.001*GDS_LatLon_Lo1(gds), 0.001*GDS_LatLon_Lo2(gds), 
		  0.001*GDS_LatLon_dx(gds),
	    	  nx, ny, GDS_LatLon_scan(gds), GDS_LatLon_mode(gds),
		  BDS_Grid(bds));
	    else if (gds && GDS_Gaussian(gds) && nx == -1) {
		printf("  thinned gaussian: lat  %f to %f\n"
                       "     lon %f   %ld grid pts   (%d x %d) scan %d"
			" mode %d bdsgrid %d  nlat:\n",
		  0.001*GDS_LatLon_La1(gds), 0.001*GDS_LatLon_La2(gds),
		  0.001*GDS_LatLon_Lo1(gds),
	    	  nxny, nx, ny, GDS_LatLon_scan(gds), GDS_LatLon_mode(gds),
		  BDS_Grid(bds));
		  GDS_prt_thin_lon(gds);
	    }
	    else if (gds && GDS_Polar(gds))
		printf("  polar stereo: Lat1 %f Long1 %f Orient %f\n"
			"     %s pole (%d x %d) Dx %d Dy %d scan %d mode %d\n",
		    0.001*GDS_Polar_La1(gds),0.001*GDS_Polar_Lo1(gds),
		    0.001*GDS_Polar_Lov(gds),
		    GDS_Polar_pole(gds) == 0 ? "north" : "south", nx,ny,
		    GDS_Polar_Dx(gds),GDS_Polar_Dy(gds),
		    GDS_Polar_scan(gds), GDS_Polar_mode(gds));
	    else if (gds && GDS_Lambert(gds))
		printf("  Lambert Conf: Lat1 %f Lon1 %f Lov %f\n"
                       "      Latin1 %f Latin2 %f LatSP %f LonSP %f\n"
                       "      %s (%d x %d) Dx %f Dy %f scan %d mode %d\n",
                     0.001*GDS_Lambert_La1(gds),0.001*GDS_Lambert_Lo1(gds),
                     0.001*GDS_Lambert_Lov(gds),
                     0.001*GDS_Lambert_Latin1(gds), 0.001*GDS_Lambert_Latin2(gds),
                     0.001*GDS_Lambert_LatSP(gds), 0.001*GDS_Lambert_LonSP(gds),
                      GDS_Lambert_NP(gds) ? "North Pole": "South Pole",
                     GDS_Lambert_nx(gds), GDS_Lambert_ny(gds),
                     0.001*GDS_Lambert_dx(gds), 0.001*GDS_Lambert_dy(gds),
                     GDS_Lambert_scan(gds), GDS_Lambert_mode(gds));
	    else if (gds && GDS_Mercator(gds))
		printf("  Mercator: lat  %f to %f by %f km  nxny %ld\n"
                       "          long %f to %f by %f km, (%d x %d) scan %d"
			" mode %d Latin %f bdsgrid %d\n",
		  0.001*GDS_Merc_La1(gds), 0.001*GDS_Merc_La2(gds),
		  0.001*GDS_Merc_dy(gds), nxny, 0.001*GDS_Merc_Lo1(gds),
		  0.001*GDS_Merc_Lo2(gds), 0.001*GDS_Merc_dx(gds),
	    	  nx, ny, GDS_Merc_scan(gds), GDS_Merc_mode(gds), 
		  0.001*GDS_Merc_Latin(gds), BDS_Grid(bds));
	    else if (gds && GDS_ssEgrid(gds))
		printf("  Semi-staggered Arakawa E-Grid: lat0 %f lon0 %f nxny %d\n"
                       "    dLat %f dLon %f (%d x %d) scan %d mode %d\n",
		  0.001*GDS_ssEgrid_La1(gds), 0.001*GDS_ssEgrid_Lo1(gds), 
                  GDS_ssEgrid_n(gds)*GDS_ssEgrid_n_dum(gds), 
                  0.001*GDS_ssEgrid_dj(gds), 0.001*GDS_ssEgrid_di(gds), 
                  GDS_ssEgrid_Lo2(gds), GDS_ssEgrid_La2(gds),
                  GDS_ssEgrid_scan(gds), GDS_ssEgrid_mode(gds));
            else if (gds && GDS_ss2dEgrid(gds))
                printf("  Semi-staggered Arakawa E-Grid (2D): lat0 %f lon0 %f nxny %d\n"
                       "    dLat %f dLon %f (tlm0d %f tph0d %f) scan %d mode %d\n",
                   0.001*GDS_ss2dEgrid_La1(gds), 0.001*GDS_ss2dEgrid_Lo1(gds),
                   GDS_ss2dEgrid_nx(gds)*GDS_ss2dEgrid_ny(gds),
                   0.001*GDS_ss2dEgrid_dj(gds), 0.001*GDS_ss2dEgrid_di(gds),
                   0.001*GDS_ss2dEgrid_Lo2(gds), 0.001*GDS_ss2dEgrid_La2(gds),
                   GDS_ss2dEgrid_scan(gds), GDS_ss2dEgrid_mode(gds));
	    else if (gds && GDS_fEgrid(gds)) 
		printf("  filled Arakawa E-Grid: lat0 %f lon0 %f nxny %d\n"
                       "    dLat %f dLon %f (%d x %d) scan %d mode %d\n",
		  0.001*GDS_fEgrid_La1(gds), 0.001*GDS_fEgrid_Lo1(gds), 
                  GDS_fEgrid_n(gds)*GDS_fEgrid_n_dum(gds), 
                  0.001*GDS_fEgrid_dj(gds), 0.001*GDS_fEgrid_di(gds), 
                  GDS_fEgrid_Lo2(gds), GDS_fEgrid_La2(gds),
                  GDS_fEgrid_scan(gds), GDS_fEgrid_mode(gds));
	    else if (gds && GDS_RotLL(gds))
		printf("  rotated LatLon grid  lat %f to %f  lon %f to %f\n"
		       "    nxny %ld  (%d x %d)  dx %d dy %d  scan %d  mode %d\n"
		       "    transform: south pole lat %f lon %f  rot angle %f\n", 
		   0.001*GDS_RotLL_La1(gds), 0.001*GDS_RotLL_La2(gds), 
		   0.001*GDS_RotLL_Lo1(gds), 0.001*GDS_RotLL_Lo2(gds),
		   nxny, GDS_RotLL_nx(gds), GDS_RotLL_ny(gds),
		   GDS_RotLL_dx(gds), GDS_RotLL_dy(gds),
		   GDS_RotLL_scan(gds), GDS_RotLL_mode(gds),
		   0.001*GDS_RotLL_LaSP(gds), 0.001*GDS_RotLL_LoSP(gds),
		   GDS_RotLL_RotAng(gds) );
	    else if (gds && GDS_Gnomonic(gds))
		printf("  Gnomonic grid\n");
	    else if (gds && GDS_Harmonic(gds))
		printf("  Harmonic (spectral):  pentagonal spectral truncation: nj %d nk %d nm %d\n",
		       GDS_Harmonic_nj(gds), GDS_Harmonic_nk(gds),
		       GDS_Harmonic_nm(gds));
		if (gds && GDS_Harmonic_type(gds) == 1)
		  printf("  Associated Legendre polynomials\n");
            else if (gds && GDS_Triangular(gds))
                printf("  Triangular grid:  nd %d ni %d (= 2^%d x 3^%d)\n",
		    GDS_Triangular_nd(gds), GDS_Triangular_ni(gds), 
                    GDS_Triangular_ni2(gds), GDS_Triangular_ni3(gds) );
	    if (print_PDS || print_PDS10) 
                print_pds(pds, print_PDS, print_PDS10, verbose);
	    if (gds && (print_GDS || print_GDS10)) 
                 print_gds(gds, print_GDS, print_GDS10, verbose);
	}

	if (mode != INVENTORY && output_type == GRIB) {
	    if (header == dwd) wrtieee_header((int) len_grib, dump_file);
	    fwrite((void *) msg, sizeof(char), len_grib, dump_file);
	    if (header == dwd) wrtieee_header((int) len_grib, dump_file);
	    n_dump++;
	}

	if ((mode != INVENTORY && output_type != GRIB) || verbose > 1) {
	    /* decode numeric data */
 
            if ((array = (float *) malloc(sizeof(float) * nxny)) == NULL) {
                fprintf(stderr,"memory problems\n");
                exit(8);
            }

	    temp = int_power(10.0, - PDS_DecimalScale(pds));

 	    BDS_unpack(array, bds, BMS_bitmap(bms), BDS_NumBits(bds), nxny,
			   temp*BDS_RefValue(bds),temp*int_power(2.0, BDS_BinScale(bds)));

	    if (verbose > 1) {
		rmin = FLT_MAX;
		rmax = -FLT_MAX;
	        for (i = 0; i < nxny; i++) {
		    if (fabs(array[i]-UNDEFINED) > 0.0001*UNDEFINED) {
	                rmin = min(rmin,array[i]);
	                rmax = max(rmax,array[i]);
		    }
	        }
	        printf("  min/max data %g %g  num bits %d "
			" BDS_Ref %g  DecScale %d BinScale %d\n", 
		    rmin, rmax, BDS_NumBits(bds), BDS_RefValue(bds),
		    PDS_DecimalScale(pds), BDS_BinScale(bds));
	    }

	    if (mode != INVENTORY && output_type != GRIB) {
		/* dump code */
		if (output_PDS_GDS == 1) {
		    /* insert code here */
	            if (output_type == BINARY || output_type == IEEE) {
			/* write PDS */
			i = PDS_LEN(pds) + 4;
	                if (header == simple && output_type == BINARY) 
				fwrite((void *) &i, sizeof(int), 1, dump_file);
	                if (header == simple && output_type == IEEE) wrtieee_header(i, dump_file);
	                fwrite((void *) "PDS ", 1, 4, dump_file);
	                fwrite((void *) pds, 1, i - 4, dump_file);
	                if (header == simple && output_type == BINARY) 
				fwrite((void *) &i, sizeof(int), 1, dump_file);
	                if (header == simple && output_type == IEEE) wrtieee_header(i, dump_file);

			/* write GDS */
			i = (gds) ?  GDS_LEN(gds) + 4 : 4;
	                if (header == simple && output_type == BINARY) 
				fwrite((void *) &i, sizeof(int), 1, dump_file);
	                if (header == simple && output_type == IEEE) wrtieee_header(i, dump_file);
	                fwrite((void *) "GDS ", 1, 4, dump_file);
	                if (gds) fwrite((void *) gds, 1, i - 4, dump_file);
	                if (header == simple && output_type == BINARY) 
				fwrite((void *) &i, sizeof(int), 1, dump_file);
	                if (header == simple && output_type == IEEE) wrtieee_header(i, dump_file);
		    }
		} 

	        if (output_type == BINARY) {
	            i = nxny * sizeof(float);
	            if (header == simple) fwrite((void *) &i, sizeof(int), 1, dump_file);
	            fwrite((void *) array, sizeof(float), nxny, dump_file);
	            if (header == simple) fwrite((void *) &i, sizeof(int), 1, dump_file);
	        }
		else if (output_type == IEEE) {
		    wrtieee(array, nxny, header, dump_file);
		}
	        else if (output_type == TEXT) {
	            /* number of points in grid */
	            if (header == simple) {
		        if (nx <= 0 || ny <= 0 || nxny != nx*ny) {
                            fprintf(dump_file, "%ld %d\n", nxny, 1);
			}
			else {
			    fprintf(dump_file, "%d %d\n", nx, ny);
			}
		    }
	            for (i = 0; i < nxny; i++) {
		        fprintf(dump_file,"%g\n", array[i]);
		    }
	        }
	        n_dump++;
	    }
	    free(array);
	    if (verbose > 0) printf("\n");
	}
	    
        pos += len_grib;
        count++;
    }

    if (mode != INVENTORY) {
	if (header == dwd && output_type == GRIB) wrtieee_header(0, dump_file);
	if (ferror(dump_file)) {
		fprintf(stderr,"error writing %s\n",dump_file_name);
		exit(8);
	}
    }
    fclose(input);
    return (return_code);
}

void print_pds(unsigned char *pds, int print_PDS, int print_PDS10, int verbose) {
    int i, j;

    j = PDS_LEN(pds);
    if (verbose < 2) {
        if (print_PDS && verbose < 2) {
            printf(":PDS=");
            for (i = 0; i < j; i++) {
                printf("%2.2x", (int) pds[i]);
            }
        }
        if (print_PDS10 && verbose < 2) {
            printf(":PDS10=");
            for (i = 0; i < j; i++) {
                printf(" %d", (int) pds[i]);
            }
        }
    }
    else {
        if (print_PDS) {
            printf("  PDS(1..%d)=",j);
            for (i = 0; i < j; i++) {
                if (i % 20 == 0) printf("\n    %4d:",i+1);
                printf(" %3.2x", (int) pds[i]);
            }
            printf("\n");
        }
        if (print_PDS10) {
            printf("  PDS10(1..%d)=",j);
            for (i = 0; i < j; i++) {
                if (i % 20 == 0) printf("\n    %4d:",i+1);
                printf(" %3d", (int) pds[i]);
            }
            printf("\n");
        }
    }
}

void print_gds(unsigned char *gds, int print_GDS, int print_GDS10, int verbose) {
    int i, j;

    j = GDS_LEN(gds);
    if (verbose < 2) {
        if (print_GDS && verbose < 2) {
            printf(":GDS=");
            for (i = 0; i < j; i++) {
                printf("%2.2x", (int) gds[i]);
            }
        }
        if (print_GDS10 && verbose < 2) {
            printf(":GDS10=");
            for (i = 0; i < j; i++) {
                printf(" %d", (int) gds[i]);
            }
        }
    }
    else {
        if (print_GDS) {
            printf("  GDS(1..%d)=",j);
            for (i = 0; i < j; i++) {
                if (i % 20 == 0) printf("\n    %4d:",i+1);
                printf(" %3.2x", (int) gds[i]);
            }
            printf("\n");
        }
        if (print_GDS10) {
            printf("  GDS10(1..%d)=",j);
            for (i = 0; i < j; i++) {
                if (i % 20 == 0) printf("\n    %4d:",i+1);
                printf(" %3d", (int) gds[i]);
            }
            printf("\n");
        }
    }
}
/*
 * find next grib header
 *
 * file = what do you think?
 * pos = initial position to start looking at  ( = 0 for 1st call)
 *       returns with position of next grib header (units=bytes)
 * len_grib = length of the grib record (bytes)
 * buffer[buf_len] = buffer for reading/writing
 *
 * returns (char *) to start of GRIB header+PDS
 *         NULL if not found
 *
 * adapted from SKGB (Mark Iredell)
 *
 * v1.1 9/94 Wesley Ebisuzaki
 * v1.2 3/96 Wesley Ebisuzaki handles short records at end of file
 * v1.3 8/96 Wesley Ebisuzaki increase NTRY from 3 to 100 for the folks
 *      at Automation decided a 21 byte WMO bulletin header wasn't long 
 *      enough and decided to go to an 8K header.  
 * v1.4 11/10/2001 D. Haalman, looks at entire file, does not try
 *      to read past EOF
 */

#ifndef min
   #define min(a,b)  ((a) < (b) ? (a) : (b))
#endif

#define NTRY 100
/* #define LEN_HEADER_PDS (28+42+100) */
#define LEN_HEADER_PDS (28+8)

unsigned char *seek_grib(FILE *file, long *pos, long *len_grib, 
        unsigned char *buffer, unsigned int buf_len) {

    int i, j, len;

    j = 1;
    clearerr(file);
    while ( !feof(file) ) {

        if (fseek(file, *pos, SEEK_SET) == -1) break;
        i = fread(buffer, sizeof (unsigned char), buf_len, file);     
        if (ferror(file)) break;
        len = i - LEN_HEADER_PDS;
     
        for (i = 0; i < len; i++) {
            if (buffer[i] == 'G' && buffer[i+1] == 'R' && buffer[i+2] == 'I'
                && buffer[i+3] == 'B' && buffer[i+7] == 1) {
                    *pos = i + *pos;
                    *len_grib = (buffer[i+4] << 16) + (buffer[i+5] << 8) +
                            buffer[i+6];
                    return (buffer+i);
            }
        }

	if (j++ == NTRY) {
	    fprintf(stderr,"found unidentified data \n");
           /* break; // stop seeking after NTRY records */  
        }

	*pos = *pos + (buf_len - LEN_HEADER_PDS);
    }

    *len_grib = 0;
    return (unsigned char *) NULL;
}



/* ibm2flt       wesley ebisuzaki
 *
 * v1.1 .. faster
 * v1.1 .. if mant == 0 -> quick return
 *
 */


double ibm2flt(unsigned char *ibm) {

	int positive, power;
	unsigned int abspower;
	long int mant;
	double value, exp;

	mant = (ibm[1] << 16) + (ibm[2] << 8) + ibm[3];
        if (mant == 0) return 0.0;

	positive = (ibm[0] & 0x80) == 0;
	power = (int) (ibm[0] & 0x7f) - 64;
	abspower = power > 0 ? power : -power;


	/* calc exp */
	exp = 16.0;
	value = 1.0;
	while (abspower) {
		if (abspower & 1) {
			value *= exp;
		}
		exp = exp * exp;
		abspower >>= 1;
	}

	if (power < 0) value = 1.0 / value;
	value = value * mant / 16777216.0;
	if (positive == 0) value = -value;
	return value;
}
	
/*
 * read_grib.c
 *
 * reads grib message
 *
 * input: pos, byte position of grib message
 *        len_grib, length of grib message
 * output: *buffer, grib message
 *
 * note: call seek_grib first
 *
 * v1.0 9/94 Wesley Ebisuzaki
 *
 */

int read_grib(FILE *file, long pos, long len_grib, unsigned char *buffer) {

    int i;


    if (fseek(file, pos, SEEK_SET) == -1) {
	    return 0;
    }

    i = fread(buffer, sizeof (unsigned char), len_grib, file);
    return (i == len_grib);
}

/*
 * w. ebisuzaki
 *
 *  return x**y
 *
 *
 *  input: double x
 *	   int y
 */
double int_power(double x, int y) {

	double value;

	if (y < 0) {
		y = -y;
		x = 1.0 / x;
	}
	value = 1.0;

	while (y) {
		if (y & 1) {
			value *= x;
		}
		x = x * x;
		y >>= 1;
	}
	return value;
}

/* cnames.c 				Wesley Ebisuzaki
 *
 * returns strings with either variable name or comment field
 * v1.4 4/98
 * reanalysis can use process 180 and subcenter 0
 *
 * Add DWD tables 2, 201, 202, 203      Helmut P. Frank, DWD, FE13
 *                                      Thu Aug 23 09:28:34 GMT 2001
 */


extern  struct ParmTable parm_table_ncep_opn[256];
extern  struct ParmTable parm_table_ncep_reanal[256];
extern  struct ParmTable parm_table_nceptab_129[256];
extern  struct ParmTable parm_table_omb[256];
extern  struct ParmTable parm_table_nceptab_130[256];
extern  struct ParmTable parm_table_nceptab_131[256];

extern  struct ParmTable parm_table_ecmwf_128[256];
extern  struct ParmTable parm_table_ecmwf_129[256];
extern  struct ParmTable parm_table_ecmwf_130[256];
extern  struct ParmTable parm_table_ecmwf_131[256];
extern  struct ParmTable parm_table_ecmwf_140[256];
extern  struct ParmTable parm_table_ecmwf_150[256];
extern  struct ParmTable parm_table_ecmwf_151[256];
extern  struct ParmTable parm_table_ecmwf_160[256];
extern  struct ParmTable parm_table_ecmwf_170[256];
extern  struct ParmTable parm_table_ecmwf_180[256];
extern  struct ParmTable parm_table_ecmwf_190[256];
extern  struct ParmTable parm_table_user[256];
extern  struct ParmTable parm_table_dwd_002[256];
extern  struct ParmTable parm_table_dwd_201[256];
extern  struct ParmTable parm_table_dwd_202[256];
extern  struct ParmTable parm_table_dwd_203[256];
extern  struct ParmTable parm_table_cptec_254[256];

extern enum Def_NCEP_Table def_ncep_table;

/*
 * returns pointer to the parameter table
 */



static struct ParmTable *Parm_Table(unsigned char *pds) {

    int i, center, subcenter, ptable, process;
    static int missing_count = 0, reanal_opn_count = 0;

    center = PDS_Center(pds);
    subcenter = PDS_Subcenter(pds);
    ptable = PDS_Vsn(pds);

#ifdef P_TABLE_FIRST
    i = setup_user_table(center, subcenter, ptable);
    if (i == 1) return &parm_table_user[0];
#endif
    /* figure out if NCEP opn or reanalysis */
    if (center == NMC && ptable <= 3) {
	if (subcenter == 1) return &parm_table_ncep_reanal[0];
        process = PDS_Model(pds);
	if (subcenter != 0 || (process != 80 && process != 180) || 
		(ptable != 1 && ptable != 2)) 
            return &parm_table_ncep_opn[0];

	/* at this point could be either the opn or reanalysis table */
	if (def_ncep_table == opn_nowarn) return &parm_table_ncep_opn[0];
	if (def_ncep_table == rean_nowarn) return &parm_table_ncep_reanal[0];
        if (reanal_opn_count++ == 0) {
	    fprintf(stderr, "Using NCEP %s table, see -ncep_opn, -ncep_rean options\n",
               (def_ncep_table == opn) ?  "opn" : "reanalysis");
	}
        return (def_ncep_table == opn) ?  &parm_table_ncep_opn[0] 
		: &parm_table_ncep_reanal[0];
    }

    if (center == NMC) {
        if (ptable == 128) return &parm_table_omb[0];
        if (ptable == 129) return &parm_table_nceptab_129[0];
        if (ptable == 130) return &parm_table_nceptab_130[0];
        if (ptable == 131) return &parm_table_nceptab_131[0];
        if (ptable == 132) return &parm_table_ncep_reanal[0];
    }
    if (center == ECMWF) {
        if (ptable == 128) return &parm_table_ecmwf_128[0];
        if (ptable == 129) return &parm_table_ecmwf_129[0];
        if (ptable == 130) return &parm_table_ecmwf_130[0];
        if (ptable == 131) return &parm_table_ecmwf_131[0];
        if (ptable == 140) return &parm_table_ecmwf_140[0];
        if (ptable == 150) return &parm_table_ecmwf_150[0];
        if (ptable == 151) return &parm_table_ecmwf_151[0];
        if (ptable == 160) return &parm_table_ecmwf_160[0];
        if (ptable == 170) return &parm_table_ecmwf_170[0];
        if (ptable == 180) return &parm_table_ecmwf_180[0];
        if (ptable == 190) return &parm_table_ecmwf_190[0];
    }
    if (center == DWD) {
        if (ptable ==   2) return &parm_table_dwd_002[0];
        if (ptable == 201) return &parm_table_dwd_201[0];
        if (ptable == 202) return &parm_table_dwd_202[0];
        if (ptable == 203) return &parm_table_dwd_203[0];
    }
    if (center == CPTEC) {
	if (ptable == 254) return &parm_table_cptec_254[0];
    }

#ifndef P_TABLE_FIRST
    i = setup_user_table(center, subcenter, ptable);
    if (i == 1) return &parm_table_user[0];
#endif

    if ((ptable > 3 || (PDS_PARAM(pds)) > 127) && missing_count++ == 0) {
	fprintf(stderr,
            "\nUndefined parameter table (center %d-%d table %d), using NCEP-opn\n",
            center, subcenter, ptable);
    }
    return &parm_table_ncep_opn[0];
}

/*
 * return name field of PDS_PARAM(pds)
 */

char *k5toa(unsigned char *pds) {

    return (Parm_Table(pds) + PDS_PARAM(pds))->name;
}

/*
 * return comment field of the PDS_PARAM(pds)
 */

char *k5_comments(unsigned char *pds) {

    return (Parm_Table(pds) + PDS_PARAM(pds))->comment;
}

/* 1996				wesley ebisuzaki
 *
 * Unpack BDS section
 *
 * input: *bits, pointer to packed integer data
 *        *bitmap, pointer to bitmap (undefined data), NULL if none
 *        n_bits, number of bits per packed integer
 *        n, number of data points (includes undefined data)
 *        ref, scale: flt[] = ref + scale*packed_int
 * output: *flt, pointer to output array
 *        undefined values filled with UNDEFINED
 *
 * note: code assumes an integer > 32 bits
 *
 * 7/98 v1.2.1 fix bug for bitmaps and nbit >= 25 found by Larry Brasfield
 * 2/01 v1.2.2 changed jj from long int to double
 * 3/02 v1.2.3 added unpacking extensions for spectral data 
 *             Luis Kornblueh, MPIfM 
 */

static unsigned int mask[] = {0,1,3,7,15,31,63,127,255};
static unsigned int map_masks[8] = {128, 64, 32, 16, 8, 4, 2, 1};
static double shift[9] = {1.0, 2.0, 4.0, 8.0, 16.0, 32.0, 64.0, 128.0, 256.0};

void BDS_unpack(float *flt, unsigned char *bds, unsigned char *bitmap,
	int n_bits, int n, double ref, double scale) {

    unsigned char *bits;

    int i, mask_idx, t_bits, c_bits, j_bits;
    unsigned int j, map_mask, tbits, jmask, bbits;
    double jj;

    if (BDS_Harmonic(bds)) {
        bits = bds + 15;
        /* fill in global mean */
        *flt++ = BDS_Harmonic_RefValue(bds);
        n -= 1; 
    }
    else {
        bits = bds + 11;  
    }

    tbits = bbits = 0;

    /* assume integer has 32+ bits */
    if (n_bits <= 25) {
        jmask = (1 << n_bits) - 1;
        t_bits = 0;

        if (bitmap) {
	    for (i = 0; i < n; i++) {
		/* check bitmap */
		mask_idx = i & 7;
		if (mask_idx == 0) bbits = *bitmap++;
	        if ((bbits & map_masks[mask_idx]) == 0) {
		    *flt++ = UNDEFINED;
		    continue;
	        }

	        while (t_bits < n_bits) {
	            tbits = (tbits * 256) + *bits++;
	            t_bits += 8;
	        }
	        t_bits -= n_bits;
	        j = (tbits >> t_bits) & jmask;
	        *flt++ = ref + scale*j;
            }
        }
        else {
	    for (i = 0; i < n; i++) {
                while (t_bits < n_bits) {
                    tbits = (tbits * 256) + *bits++;
                    t_bits += 8;
                }
                t_bits -= n_bits;
                flt[i] = (tbits >> t_bits) & jmask;
            }
	    /* at least this vectorizes :) */
	    for (i = 0; i < n; i++) {
		flt[i] = ref + scale*flt[i];
	    }
        }
    }
    else {
	/* older unoptimized code, not often used */
        c_bits = 8;
        map_mask = 128;
        while (n-- > 0) {
	    if (bitmap) {
	        j = (*bitmap & map_mask);
	        if ((map_mask >>= 1) == 0) {
		    map_mask = 128;
		    bitmap++;
	        }
	        if (j == 0) {
		    *flt++ = UNDEFINED;
		    continue;
	        }
	    }

	    jj = 0.0;
	    j_bits = n_bits;
	    while (c_bits <= j_bits) {
	        if (c_bits == 8) {
		    jj = jj * 256.0  + (double) (*bits++);
		    j_bits -= 8;
	        }
	        else {
		    jj = (jj * shift[c_bits]) + (double) (*bits & mask[c_bits]);
		    bits++;
		    j_bits -= c_bits;
		    c_bits = 8;
	        }
	    }
	    if (j_bits) {
	        c_bits -= j_bits;
	        jj = (jj * shift[j_bits]) + (double) ((*bits >> c_bits) & mask[j_bits]);
	    }
	    *flt++ = ref + scale*jj;
        }
    }
    return;
}

/*
 * convert a float to an ieee single precision number v1.1
 * (big endian)
 *                      Wesley Ebisuzaki
 *
 * bugs: doesn't handle subnormal numbers
 * bugs: assumes length of integer >= 25 bits
 */

int flt2ieee(float x, unsigned char *ieee) {

	int sign, exp;
        unsigned int umant;
	double mant;

	if (x == 0.0) {
		ieee[0] = ieee[1] = ieee[2] = ieee[3] = 0;
		return 0;
	}

	/* sign bit */
	if (x < 0.0) {
		sign = 128;
		x = -x;
	}
	else sign = 0;
	mant = frexp((double) x, &exp);

        /* 2^24 = 16777216 */

	umant = mant * 16777216 + 0.5;
	if (umant >= 16777216) {
            umant = umant / 2;
            exp++;
        }
        /* bit 24 should be a 1 .. not used in ieee format */

	exp = exp - 1 + 127;

	if (exp < 0) {
		/* signed zero */
		ieee[0] = sign;
		ieee[1] = ieee[2] = ieee[3] = 0;
		return 0;
	}
	if (exp > 255) {
		/* signed infinity */
		ieee[0] = sign + 127;
		ieee[1] = 128;
                ieee[2] = ieee[3] = 0;
                return 0;
	}
	/* normal number */

	ieee[0] = sign + (exp >> 1);

        ieee[3] = umant & 255;
        ieee[2] = (umant >> 8) & 255;
        ieee[1] = ((exp & 1) << 7) + ((umant >> 16) & 127);
	return 0;
}


/* wesley ebisuzaki v1.3
 *
 * write ieee file -- big endian format
 *
 * input float *array		data to be written
 *	 int n			size of array
 *	 int header		1 for f77 style header 0 for none
 *				(header is 4 byte header
 *	 FILE *output		output file
 *
 * v1.2 7/97 buffered, faster
 * v1.3 2/99 fixed (typo) error in wrtieee_header found by
 *     Bob Farquhar
 */

#define BSIZ 1024*4

int wrtieee(float *array, int n, int header, FILE *output) {

	unsigned long int l;
	int i, nbuf;
	unsigned char buff[BSIZ];
	unsigned char h4[4];

	nbuf = 0;
	if (header) {
		l = n * 4;
		for (i = 0; i < 4; i++) {
			h4[i] = l & 255;
			l >>= 8;
		}
		buff[nbuf++] = h4[3];
		buff[nbuf++] = h4[2];
		buff[nbuf++] = h4[1];
		buff[nbuf++] = h4[0];
	}
	for (i = 0; i < n; i++) {
		if (nbuf >= BSIZ) {
		    fwrite(buff, 1, BSIZ, output);
		    nbuf = 0;
		}
		flt2ieee(array[i], buff + nbuf);
		nbuf += 4;
	}
	if (header) {
		if (nbuf == BSIZ) {
		    fwrite(buff, 1, BSIZ, output);
		    nbuf = 0;
		}
		buff[nbuf++] = h4[3];
		buff[nbuf++] = h4[2];
		buff[nbuf++] = h4[1];
		buff[nbuf++] = h4[0];
	}
	if (nbuf) fwrite(buff, 1, nbuf, output);
	return 0;
}

/* write a big-endian 4 byte integer .. f77 IEEE  header */

int wrtieee_header(unsigned int n, FILE *output) {
	unsigned h4[4];

	h4[0] = n & 255;
	h4[1] = (n >> 8) & 255;
	h4[2] = (n >> 16) & 255;
	h4[3] = (n >> 24) & 255;

	putc(h4[3],output);
	putc(h4[2],output);
	putc(h4[1],output);
	putc(h4[0],output);

	return 0;
}


/* wesley ebisuzaki v1.0
 *
 * levels.c
 *
 * prints out a simple description of kpds6, kpds7
 *    (level/layer data)
 *  kpds6 = octet 10 of the PDS
 *  kpds7 = octet 11 and 12 of the PDS
 *    (kpds values are from NMC's grib routines)
 *  center = PDS_Center(pds) .. NMC, ECMWF, etc
 *
 * the description of the levels is 
 *   (1) incomplete
 *   (2) include some NMC-only values (>= 200?)
 *
 * v1.1 wgrib v1.7.3.1 updated with new levels
 * v1.2 added new level and new parameter
 * v1.2.1 modified level 117 pv units
 * v1.2.2 corrected level 141
 * v1.2.3 fixed layer 206 (was 205)
 */

void levels(int kpds6, int kpds7, int center) {

	int o11, o12;

	/* octets 11 and 12 */
	o11 = kpds7 / 256;
	o12 = kpds7 % 256;


	switch (kpds6) {

	case 1: printf("sfc");
		break;
	case 2: printf("cld base");
		break;
	case 3: printf("cld top");
		break;
	case 4: printf("0C isotherm");
		break;
	case 5: printf("cond lev");
		break;
	case 6: printf("max wind lev");
		break;
	case 7: printf("tropopause");
		break;
	case 8: printf("nom. top");
		break;
	case 9: printf("sea bottom");
		break;
	case 200:
	case 10: printf("atmos col");
		break;

	case 12:
	case 212: printf("low cld bot");
		break;
	case 13:
	case 213: printf("low cld top");
		break;
	case 14:
	case 214: printf("low cld lay");
		break;
	case 22:
	case 222: printf("mid cld bot");
		break;
	case 23:
	case 223: printf("mid cld top");
		break;
	case 24:
	case 224: printf("mid cld lay");
		break;
	case 32:
	case 232: printf("high cld bot");
		break;
	case 33:
	case 233: printf("high cld top");
		break;
	case 34:
	case 234: printf("high cld lay");
		break;

	case 201: printf("ocean column");
		break;
	case 204: printf("high trop freezing lvl");
		break;
	case 206: printf("grid-scale cld bot");
		break;
	case 207: printf("grid-scale cld top");
		break;
	case 209: printf("bndary-layer cld bot");
		break;
	case 210: printf("bndary-layer cld top");
		break;
	case 211: printf("bndary-layer cld layer");
		break;
	case 235: if (kpds7 % 10 == 0)
		printf("%dC ocean isotherm level",kpds7/10);
		else printf("%.1fC ocean isotherm level",kpds7/10.0);
		break;
	case 236: printf("%d-%dm ocean layer",o11*10,o12*10);
		break;
	case 237: printf("ocean mixed layer bot");
		break;
	case 238: printf("ocean isothermal layer bot");
		break;
	case 242: printf("convect-cld bot");
		break;
	case 243: printf("convect-cld top");
		break;
	case 244: printf("convect-cld layer");
		break;
	case 246: printf("max e-pot-temp lvl");
		break;
	case 247: printf("equilibrium lvl");
		break;
	case 248: printf("shallow convect-cld bot");
		break;
	case 249: printf("shallow convect-cld top");
		break;
	case 251: printf("deep convect-cld bot");
		break;
	case 252: printf("deep convect-cld top");
		break;

	case 100: printf("%d mb",kpds7);
	 	break;
	case 101: printf("%d-%d mb",o11*10,o12*10);
	 	break;
	case 102: printf("MSL");
	 	break;
	case 103: printf("%d m above MSL",kpds7);
	 	break;
	case 104: printf("%d-%d m above msl",o11*100,o12*100);
	 	break;
	case 105: printf("%d m above gnd",kpds7);
	 	break;
	case 106: printf("%d-%d m above gnd",o11*100,o12*100);
	 	break;
	case 107: printf("sigma=%.4f",kpds7/10000.0);
	 	break;
	case 108: printf("sigma %.2f-%.2f",o11/100.0,o12/100.0);
	 	break;
	case 109: printf("hybrid lev %d",kpds7);
	 	break;
	case 110: printf("hybrid %d-%d",o11,o12);
	 	break;
	case 111: printf("%d cm down",kpds7);
	 	break;
	case 112: printf("%d-%d cm down",o11,o12);
	 	break;
	case 113: printf("%dK",kpds7);
	 	break;
	case 114: printf("%d-%dK",475-o11,475-o12);
	 	break;
	case 115: printf("%d mb above gnd",kpds7);
	 	break;
	case 116: printf("%d-%d mb above gnd",o11,o12);
	 	break;
	case 117: printf("%d pv units",INT2(o11,o12)); /* units are suspect */
	 	break;
	case 119: printf("%.5f (ETA level)",kpds7/10000.0);
	 	break;
	case 120: printf("%.2f-%.2f (ETA levels)",o11/100.0,o12/100.0);
	 	break;
	case 121: printf("%d-%d mb",1100-o11,1100-o12);
	 	break;
	case 125: printf("%d cm above gnd",kpds7);
	 	break;
	case 126: 
		if (center == NMC) printf("%.2f mb",kpds7*0.01);
	 	break;
	case 128: printf("%.3f-%.3f (sigma)",1.1-o11/1000.0, 1.1-o12/1000.0);
	 	break;
	case 141: printf("%d-%d mb",o11*10,1100-o12);
	 	break;
	case 160: printf("%d m below sea level",kpds7);
	 	break;
	default:
	 	break;
	}
}

/*
 * PDStimes.c   v1.2 wesley ebisuzaki
 *
 * prints something readable for time code in grib file
 *
 * not all cases decoded
 * for NCEP/NCAR Reanalysis
 *
 * v1.2.1 1/99 fixed forecast time unit table
 * v1.2.2 10/01 add time_range = 11 (at DWD)  Helmut P. Frank
 */

static char *units[] = {
	"min", "hr", "d", "mon", "yr",
	"decade", "normal", "century", "??", "??", " x3 hours", " x6 hours",
        " x12 hours",
        "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
        "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
        "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
        "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
        "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
        "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
        "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
        "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
        "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
        "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
        "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
        "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
        "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
        "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
        "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
        "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
        "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
        "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
        "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
        "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
        "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
        "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
        "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
        "??", "??", "??", "??", "??", "??", "??", "??", "??", "??",
        "??", " sec"}; 

void PDStimes(int time_range, int p1, int p2, int time_unit) {

	char *unit;
	enum {anal, fcst, unknown} type;
	int fcst_len = 0;

	if (time_unit >= 0 && time_unit <= sizeof(units)/sizeof(char *))
             unit = units[time_unit];
	else unit = "";


        /* change x3/x6/x12 to hours */

        if (time_unit == HOURS3) {
	    p1 *= 3; p2 *= 3;
	    time_unit = HOUR;
        }
        else if (time_unit == HOURS6) {
	    p1 *= 6; p2 *= 6;
	    time_unit = HOUR;
        }
        else if (time_unit == HOURS12) {
	    p1 *= 12; p2 *= 12;
	    time_unit = HOUR;
        }

	if (time_unit >= 0 && time_unit <= sizeof(units)/sizeof(char *))
             unit = units[time_unit];
	else unit = "";

	/* figure out if analysis or forecast */
	/* in GRIB, there is a difference between init and uninit analyses */
	/* not case at NMC .. no longer run initialization */
	/* ignore diff between init an uninit analyses */

	switch (time_range) {

	case 0:
	case 1:
	case 113:
	case 114:
	case 118:
		if (p1 == 0) type = anal;
		else {
			type = fcst;
			fcst_len = p1;
		}
		break;
	case 10: /* way NMC uses it, should be unknown? */
		type = fcst;
		fcst_len = p1*256 + p2;
		if (fcst_len == 0) type = anal;
		break;

	case 51:
		type = unknown;
		break;
	case 123:
	case 124:
		type = anal;
		break;

	case 135:
		type = anal;
		break;

	default: type = unknown;
		break;
	}

	/* ----------------------------------------------- */

	if (type == anal) printf("anl:");
	else if (type == fcst) printf("%d%s fcst:",fcst_len,unit);


	if (time_range == 123 || time_range == 124) {
		if (p1 != 0) printf("start@%d%s:",p1,unit);
	}


	/* print time range */


	switch (time_range) {

	case 0:
	case 1:
	case 10:
		break;
	case 2: printf("valid %d-%d%s:",p1,p2,unit);
		break;
	case 3: printf("%d-%d%s ave:",p1,p2,unit);
		break;
	case 4: printf("%d-%d%s acc:",p1,p2,unit);
		break;
	case 5: printf("%d-%d%s diff:",p1,p2,unit);
		break;
        case 6: printf("-%d to -%d %s ave:", p1,p2,unit);
                break;
        case 7: printf("-%d to %d %s ave:", p1,p2,unit);
                break;
	case 11: if (p1 > 0) {
		    printf("init fcst %d%s:",p1,unit);
		}
		else {
	            printf("time?:");
		}
		break;
	case 51: if (p1 == 0) {
		    /* printf("clim %d%s:",p2,unit); */
		    printf("0-%d%s product:ave@1yr:",p2,unit);
		}
		else if (p1 == 1) {
		    /* printf("clim (diurnal) %d%s:",p2,unit); */
		    printf("0-%d%s product:same-hour,ave@1yr:",p2,unit);
		}
		else {
		    printf("clim? p1=%d? %d%s?:",p1,p2,unit);
		}
		break;
	case 113:
	case 123:
		printf("ave@%d%s:",p2,unit);
		break;
	case 114:
	case 124:
		printf("acc@%d%s:",p2,unit);
		break;
	case 115:
		printf("ave of fcst:%d to %d%s:",p1,p2,unit);
		break;
	case 116:
		printf("acc of fcst:%d to %d%s:",p1,p2,unit);
		break;
	case 118: 
		printf("var@%d%s:",p2,unit);
		break;
	case 128:
		printf("%d-%d%s fcst acc:ave@24hr:", p1, p2, unit);
		break;
	case 129:
		printf("%d-%d%s fcst acc:ave@%d%s:", p1, p2, unit, p2-p1,unit);
		break;
	case 130:
		printf("%d-%d%s fcst ave:ave@24hr:", p1, p2, unit);
		break;
	case 131:
		printf("%d-%d%s fcst ave:ave@%d%s:", p1, p2, unit,p2-p1,unit);
		break;
		/* for CFS */
	case 132:
		printf("%d-%d%s anl:ave@1yr:", p1, p2, unit);
		break;
	case 133:
		printf("%d-%d%s fcst:ave@1yr:", p1, p2, unit);
		break;
	case 134:
		printf("%d-%d%s fcst-anl:rms@1yr:", p1, p2, unit);
		break;
	case 135:
		printf("%d-%d%s fcst-fcst_mean:rms@1yr:", p1, p2, unit);
		break;
	case 136:
		printf("%d-%d%s anl-anl_mean:rms@1yr:", p1, p2, unit);
		break;
		

	default: printf("time?:");
	}
}

/*
 *  number of missing data points w. ebisuzaki
 *
 *  v1.1: just faster my dear
 *  v1.2: just faster my dear
 *
 */

static int bitsum[256] = {
    8, 7, 7, 6, 7, 6, 6, 5, 7, 6, 6, 5, 6, 5, 5, 4, 
    7, 6, 6, 5, 6, 5, 5, 4, 6, 5, 5, 4, 5, 4, 4, 3, 
    7, 6, 6, 5, 6, 5, 5, 4, 6, 5, 5, 4, 5, 4, 4, 3, 
    6, 5, 5, 4, 5, 4, 4, 3, 5, 4, 4, 3, 4, 3, 3, 2, 
    7, 6, 6, 5, 6, 5, 5, 4, 6, 5, 5, 4, 5, 4, 4, 3, 
    6, 5, 5, 4, 5, 4, 4, 3, 5, 4, 4, 3, 4, 3, 3, 2, 
    6, 5, 5, 4, 5, 4, 4, 3, 5, 4, 4, 3, 4, 3, 3, 2, 
    5, 4, 4, 3, 4, 3, 3, 2, 4, 3, 3, 2, 3, 2, 2, 1, 
    7, 6, 6, 5, 6, 5, 5, 4, 6, 5, 5, 4, 5, 4, 4, 3, 
    6, 5, 5, 4, 5, 4, 4, 3, 5, 4, 4, 3, 4, 3, 3, 2, 
    6, 5, 5, 4, 5, 4, 4, 3, 5, 4, 4, 3, 4, 3, 3, 2, 
    5, 4, 4, 3, 4, 3, 3, 2, 4, 3, 3, 2, 3, 2, 2, 1, 
    6, 5, 5, 4, 5, 4, 4, 3, 5, 4, 4, 3, 4, 3, 3, 2, 
    5, 4, 4, 3, 4, 3, 3, 2, 4, 3, 3, 2, 3, 2, 2, 1, 
    5, 4, 4, 3, 4, 3, 3, 2, 4, 3, 3, 2, 3, 2, 2, 1, 
    4, 3, 3, 2, 3, 2, 2, 1, 3, 2, 2, 1, 2, 1, 1, 0};


int missing_points(unsigned char *bitmap, int n) {

    int count;
    unsigned int tmp;
    if (bitmap == NULL) return 0;

    count = 0;
    while (n >= 8) {
	tmp = *bitmap++;
	n -= 8;
        count += bitsum[tmp];
    }
    tmp = *bitmap | ((1 << (8 - n)) - 1);
    count += bitsum[tmp];

    return count;
}

/*
 * parameter table for NCEP (operations)
 * center = 7, subcenter != 2 parameter table = 1, 2, 3 etc
 * note: see reanalysis parameter table for problems
 * updated 3/2003
 */

struct ParmTable parm_table_ncep_opn[256] = {
      /* 0 */ {"var0", "undefined"},
      /* 1 */ {"PRES", "Pressure [Pa]"},
      /* 2 */ {"PRMSL", "Pressure reduced to MSL [Pa]"},
      /* 3 */ {"PTEND", "Pressure tendency [Pa/s]"},
      /* 4 */ {"PVORT", "Pot. vorticity [km^2/kg/s]"},
      /* 5 */ {"ICAHT", "ICAO Standard Atmosphere Reference Height [M]"},
      /* 6 */ {"GP", "Geopotential [m^2/s^2]"},
      /* 7 */ {"HGT", "Geopotential height [gpm]"},
      /* 8 */ {"DIST", "Geometric height [m]"},
      /* 9 */ {"HSTDV", "Std dev of height [m]"},
      /* 10 */ {"TOZNE", "Total ozone [Dobson]"},
      /* 11 */ {"TMP", "Temp. [K]"},
      /* 12 */ {"VTMP", "Virtual temp. [K]"},
      /* 13 */ {"POT", "Potential temp. [K]"},
      /* 14 */ {"EPOT", "Pseudo-adiabatic pot. temp. [K]"},
      /* 15 */ {"TMAX", "Max. temp. [K]"},
      /* 16 */ {"TMIN", "Min. temp. [K]"},
      /* 17 */ {"DPT", "Dew point temp. [K]"},
      /* 18 */ {"DEPR", "Dew point depression [K]"},
      /* 19 */ {"LAPR", "Lapse rate [K/m]"},
      /* 20 */ {"VIS", "Visibility [m]"},
      /* 21 */ {"RDSP1", "Radar spectra (1) [non-dim]"},
      /* 22 */ {"RDSP2", "Radar spectra (2) [non-dim]"},
      /* 23 */ {"RDSP3", "Radar spectra (3) [non-dim]"},
      /* 24 */ {"PLI", "Parcel lifted index (to 500 hPa) [K]"},
      /* 25 */ {"TMPA", "Temp. anomaly [K]"},
      /* 26 */ {"PRESA", "Pressure anomaly [Pa]"},
      /* 27 */ {"GPA", "Geopotential height anomaly [gpm]"},
      /* 28 */ {"WVSP1", "Wave spectra (1) [non-dim]"},
      /* 29 */ {"WVSP2", "Wave spectra (2) [non-dim]"},
      /* 30 */ {"WVSP3", "Wave spectra (3) [non-dim]"},
      /* 31 */ {"WDIR", "Wind direction [deg]"},
      /* 32 */ {"WIND", "Wind speed [m/s]"},
      /* 33 */ {"UGRD", "u wind [m/s]"},
      /* 34 */ {"VGRD", "v wind [m/s]"},
      /* 35 */ {"STRM", "Stream function [m^2/s]"},
      /* 36 */ {"VPOT", "Velocity potential [m^2/s]"},
      /* 37 */ {"MNTSF", "Montgomery stream function [m^2/s^2]"},
      /* 38 */ {"SGCVV", "Sigma coord. vertical velocity [/s]"},
      /* 39 */ {"VVEL", "Pressure vertical velocity [Pa/s]"},
      /* 40 */ {"DZDT", "Geometric vertical velocity [m/s]"},
      /* 41 */ {"ABSV", "Absolute vorticity [/s]"},
      /* 42 */ {"ABSD", "Absolute divergence [/s]"},
      /* 43 */ {"RELV", "Relative vorticity [/s]"},
      /* 44 */ {"RELD", "Relative divergence [/s]"},
      /* 45 */ {"VUCSH", "Vertical u shear [/s]"},
      /* 46 */ {"VVCSH", "Vertical v shear [/s]"},
      /* 47 */ {"DIRC", "Direction of current [deg]"},
      /* 48 */ {"SPC", "Speed of current [m/s]"},
      /* 49 */ {"UOGRD", "u of current [m/s]"},
      /* 50 */ {"VOGRD", "v of current [m/s]"},
      /* 51 */ {"SPFH", "Specific humidity [kg/kg]"},
      /* 52 */ {"RH", "Relative humidity [%]"},
      /* 53 */ {"MIXR", "Humidity mixing ratio [kg/kg]"},
      /* 54 */ {"PWAT", "Precipitable water [kg/m^2]"},
      /* 55 */ {"VAPP", "Vapor pressure [Pa]"},
      /* 56 */ {"SATD", "Saturation deficit [Pa]"},
      /* 57 */ {"EVP", "Evaporation [kg/m^2]"},
      /* 58 */ {"CICE", "Cloud Ice [kg/m^2]"},
      /* 59 */ {"PRATE", "Precipitation rate [kg/m^2/s]"},
      /* 60 */ {"TSTM", "Thunderstorm probability [%]"},
      /* 61 */ {"APCP", "Total precipitation [kg/m^2]"},
      /* 62 */ {"NCPCP", "Large scale precipitation [kg/m^2]"},
      /* 63 */ {"ACPCP", "Convective precipitation [kg/m^2]"},
      /* 64 */ {"SRWEQ", "Snowfall rate water equiv. [kg/m^2/s]"},
      /* 65 */ {"WEASD", "Accum. snow [kg/m^2]"},
      /* 66 */ {"SNOD", "Snow depth [m]"},
      /* 67 */ {"MIXHT", "Mixed layer depth [m]"},
      /* 68 */ {"TTHDP", "Transient thermocline depth [m]"},
      /* 69 */ {"MTHD", "Main thermocline depth [m]"},
      /* 70 */ {"MTHA", "Main thermocline anomaly [m]"},
      /* 71 */ {"TCDC", "Total cloud cover [%]"},
      /* 72 */ {"CDCON", "Convective cloud cover [%]"},
      /* 73 */ {"LCDC", "Low level cloud cover [%]"},
      /* 74 */ {"MCDC", "Mid level cloud cover [%]"},
      /* 75 */ {"HCDC", "High level cloud cover [%]"},
      /* 76 */ {"CWAT", "Cloud water [kg/m^2]"},
      /* 77 */ {"BLI", "Best lifted index (to 500 hPa) [K]"},
      /* 78 */ {"SNOC", "Convective snow [kg/m^2]"},
      /* 79 */ {"SNOL", "Large scale snow [kg/m^2]"},
      /* 80 */ {"WTMP", "Water temp. [K]"},
      /* 81 */ {"LAND", "Land cover (land=1;sea=0) [fraction]"},
      /* 82 */ {"DSLM", "Deviation of sea level from mean [m]"},
      /* 83 */ {"SFCR", "Surface roughness [m]"},
      /* 84 */ {"ALBDO", "Albedo [%]"},
      /* 85 */ {"TSOIL", "Soil temp. [K]"},
      /* 86 */ {"SOILM", "Soil moisture content [kg/m^2]"},
      /* 87 */ {"VEG", "Vegetation [%]"},
      /* 88 */ {"SALTY", "Salinity [kg/kg]"},
      /* 89 */ {"DEN", "Density [kg/m^3]"},
      /* 90 */ {"WATR", "Water runoff [kg/m^2]"},
      /* 91 */ {"ICEC", "Ice concentration (ice=1;no ice=0) [fraction]"},
      /* 92 */ {"ICETK", "Ice thickness [m]"},
      /* 93 */ {"DICED", "Direction of ice drift [deg]"},
      /* 94 */ {"SICED", "Speed of ice drift [m/s]"},
      /* 95 */ {"UICE", "u of ice drift [m/s]"},
      /* 96 */ {"VICE", "v of ice drift [m/s]"},
      /* 97 */ {"ICEG", "Ice growth rate [m/s]"},
      /* 98 */ {"ICED", "Ice divergence [/s]"},
      /* 99 */ {"SNOM", "Snow melt [kg/m^2]"},
      /* 100 */ {"HTSGW", "Sig height of wind waves and swell [m]"},
      /* 101 */ {"WVDIR", "Direction of wind waves [deg]"},
      /* 102 */ {"WVHGT", "Sig height of wind waves [m]"},
      /* 103 */ {"WVPER", "Mean period of wind waves [s]"},
      /* 104 */ {"SWDIR", "Direction of swell waves [deg]"},
      /* 105 */ {"SWELL", "Sig height of swell waves [m]"},
      /* 106 */ {"SWPER", "Mean period of swell waves [s]"},
      /* 107 */ {"DIRPW", "Primary wave direction [deg]"},
      /* 108 */ {"PERPW", "Primary wave mean period [s]"},
      /* 109 */ {"DIRSW", "Secondary wave direction [deg]"},
      /* 110 */ {"PERSW", "Secondary wave mean period [s]"},
      /* 111 */ {"NSWRS", "Net short wave (surface) [W/m^2]"},
      /* 112 */ {"NLWRS", "Net long wave (surface) [W/m^2]"},
      /* 113 */ {"NSWRT", "Net short wave (top) [W/m^2]"},
      /* 114 */ {"NLWRT", "Net long wave (top) [W/m^2]"},
      /* 115 */ {"LWAVR", "Long wave [W/m^2]"},
      /* 116 */ {"SWAVR", "Short wave [W/m^2]"},
      /* 117 */ {"GRAD", "Global radiation [W/m^2]"},
      /* 118 */ {"BRTMP", "Brightness temperature [K]"},
      /* 119 */ {"LWRAD", "Radiance with respect to wave no. [W/m/sr]"},
      /* 120 */ {"SWRAD", "Radiance with respect ot wave len. [W/m^3/sr]"},
      /* 121 */ {"LHTFL", "Latent heat flux [W/m^2]"},
      /* 122 */ {"SHTFL", "Sensible heat flux [W/m^2]"},
      /* 123 */ {"BLYDP", "Boundary layer dissipation [W/m^2]"},
      /* 124 */ {"UFLX", "Zonal momentum flux [N/m^2]"},
      /* 125 */ {"VFLX", "Meridional momentum flux [N/m^2]"},
      /* 126 */ {"WMIXE", "Wind mixing energy [J]"},
      /* 127 */ {"IMGD", "Image data []"},
      /* 128 */ {"MSLSA", "Mean sea level pressure (Std Atm) [Pa]"},
      /* 129 */ {"MSLMA", "Mean sea level pressure (MAPS) [Pa]"},
      /* 130 */ {"MSLET", "Mean sea level pressure (ETA model) [Pa]"},
      /* 131 */ {"LFTX", "Surface lifted index [K]"},
      /* 132 */ {"4LFTX", "Best (4-layer) lifted index [K]"},
      /* 133 */ {"KX", "K index [K]"},
      /* 134 */ {"SX", "Sweat index [K]"},
      /* 135 */ {"MCONV", "Horizontal moisture divergence [kg/kg/s]"},
      /* 136 */ {"VWSH", "Vertical speed shear [1/s]"},
      /* 137 */ {"TSLSA", "3-hr pressure tendency (Std Atmos Red) [Pa/s]"},
      /* 138 */ {"BVF2", "Brunt-Vaisala frequency^2 [1/s^2]"},
      /* 139 */ {"PVMW", "Potential vorticity (mass-weighted) [1/s/m]"},
      /* 140 */ {"CRAIN", "Categorical rain [yes=1;no=0]"},
      /* 141 */ {"CFRZR", "Categorical freezing rain [yes=1;no=0]"},
      /* 142 */ {"CICEP", "Categorical ice pellets [yes=1;no=0]"},
      /* 143 */ {"CSNOW", "Categorical snow [yes=1;no=0]"},
      /* 144 */ {"SOILW", "Volumetric soil moisture [fraction]"},
      /* 145 */ {"PEVPR", "Potential evaporation rate [W/m^2]"},
      /* 146 */ {"CWORK", "Cloud work function [J/kg]"},
      /* 147 */ {"U-GWD", "Zonal gravity wave stress [N/m^2]"},
      /* 148 */ {"V-GWD", "Meridional gravity wave stress [N/m^2]"},
      /* 149 */ {"PV", "Potential vorticity [m^2/s/kg]"},
      /* 150 */ {"COVMZ", "Covariance between u and v [m^2/s^2]"},
      /* 151 */ {"COVTZ", "Covariance between u and T [K*m/s]"},
      /* 152 */ {"COVTM", "Covariance between v and T [K*m/s]"},
      /* 153 */ {"CLWMR", "Cloud water [kg/kg]"},
      /* 154 */ {"O3MR", "Ozone mixing ratio [kg/kg]"},
      /* 155 */ {"GFLUX", "Ground heat flux [W/m^2]"},
      /* 156 */ {"CIN", "Convective inhibition [J/kg]"},
      /* 157 */ {"CAPE", "Convective Avail. Pot. Energy [J/kg]"},
      /* 158 */ {"TKE", "Turbulent kinetic energy [J/kg]"},
      /* 159 */ {"CONDP", "Lifted parcel condensation pressure [Pa]"},
      /* 160 */ {"CSUSF", "Clear sky upward solar flux [W/m^2]"},
      /* 161 */ {"CSDSF", "Clear sky downward solar flux [W/m^2]"},
      /* 162 */ {"CSULF", "Clear sky upward long wave flux [W/m^2]"},
      /* 163 */ {"CSDLF", "Clear sky downward long wave flux [W/m^2]"},
      /* 164 */ {"CFNSF", "Cloud forcing net solar flux [W/m^2]"},
      /* 165 */ {"CFNLF", "Cloud forcing net long wave flux [W/m^2]"},
      /* 166 */ {"VBDSF", "Visible beam downward solar flux [W/m^2]"},
      /* 167 */ {"VDDSF", "Visible diffuse downward solar flux [W/m^2]"},
      /* 168 */ {"NBDSF", "Near IR beam downward solar flux [W/m^2]"},
      /* 169 */ {"NDDSF", "Near IR diffuse downward solar flux [W/m^2]"},
      /* 170 */ {"RWMR", "Rain water mixing ratio [kg/kg]"},
      /* 171 */ {"SNMR", "Snow mixing ratio [kg/kg]"},
      /* 172 */ {"MFLX", "Momentum flux [N/m^2]"},
      /* 173 */ {"LMH", "Mass point model surface [non-dim]"},
      /* 174 */ {"LMV", "Velocity point model surface [non-dim]"},
      /* 175 */ {"MLYNO", "Model layer number (from bottom up) [non-dim]"},
      /* 176 */ {"NLAT", "Latitude (-90 to +90) [deg]"},
      /* 177 */ {"ELON", "East longitude (0-360) [deg]"},
      /* 178 */ {"ICMR", "Ice mixing ratio [kg/kg]"},
      /* 179 */ {"GRMR", "Graupel mixing ratio [kg/kg]"},
      /* 180 */ {"GUST", "Surface wind gust [m/s]"},
      /* 181 */ {"LPSX", "x-gradient of log pressure [1/m]"},
      /* 182 */ {"LPSY", "y-gradient of log pressure [1/m]"},
      /* 183 */ {"HGTX", "x-gradient of height [m/m]"},
      /* 184 */ {"HGTY", "y-gradient of height [m/m]"},
      /* 185 */ {"TURB", "Turbulence SIGMET/AIRMET [non-dim]"},
      /* 186 */ {"ICNG", "Icing SIGMET/AIRMET [non-dim]"},
      /* 187 */ {"LTNG", "Lightning [non-dim]"},
      /* 188 */ {"DRIP", "Rate of water dropping from canopy to gnd [kg/m^2]"},
      /* 189 */ {"VPTMP", "Virtual pot. temp. [K]"},
      /* 190 */ {"HLCY", "Storm relative helicity [m^2/s^2]"},
      /* 191 */ {"PROB", "Prob. from ensemble [non-dim]"},
      /* 192 */ {"PROBN", "Prob. from ensemble norm. to clim. expect. [non-dim]"},
      /* 193 */ {"POP", "Prob. of precipitation [%]"},
      /* 194 */ {"CPOFP", "Prob. of frozen precipitation [%]"},
      /* 195 */ {"CPOZP", "Prob. of freezing precipitation [%]"},
      /* 196 */ {"USTM", "u-component of storm motion [m/s]"},
      /* 197 */ {"VSTM", "v-component of storm motion [m/s]"},
      /* 198 */ {"NCIP", "No. concen. ice particles []"},
      /* 199 */ {"EVBS", "Direct evaporation from bare soil [W/m^2]"},
      /* 200 */ {"EVCW", "Canopy water evaporation [W/m^2]"},
      /* 201 */ {"ICWAT", "Ice-free water surface [%]"},
      /* 202 */ {"CWDI", "Convective weather detection index []"},
      /* 203 */ {"VAFTAD", "VAFTAD?? [??]"},
      /* 204 */ {"DSWRF", "Downward short wave flux [W/m^2]"},
      /* 205 */ {"DLWRF", "Downward long wave flux [W/m^2]"},
      /* 206 */ {"UVI", "Ultraviolet index [W/m^2]"},
      /* 207 */ {"MSTAV", "Moisture availability [%]"},
      /* 208 */ {"SFEXC", "Exchange coefficient [(kg/m^3)(m/s)]"},
      /* 209 */ {"MIXLY", "No. of mixed layers next to surface [integer]"},
      /* 210 */ {"TRANS", "Transpiration [W/m^2]"},
      /* 211 */ {"USWRF", "Upward short wave flux [W/m^2]"},
      /* 212 */ {"ULWRF", "Upward long wave flux [W/m^2]"},
      /* 213 */ {"CDLYR", "Non-convective cloud [%]"},
      /* 214 */ {"CPRAT", "Convective precip. rate [kg/m^2/s]"},
      /* 215 */ {"TTDIA", "Temp. tendency by all physics [K/s]"},
      /* 216 */ {"TTRAD", "Temp. tendency by all radiation [K/s]"},
      /* 217 */ {"TTPHY", "Temp. tendency by non-radiation physics [K/s]"},
      /* 218 */ {"PREIX", "Precip index (0.0-1.00) [fraction]"},
      /* 219 */ {"TSD1D", "Std. dev. of IR T over 1x1 deg area [K]"},
      /* 220 */ {"NLGSP", "Natural log of surface pressure [ln(kPa)]"},
      /* 221 */ {"HPBL", "Planetary boundary layer height [m]"},
      /* 222 */ {"5WAVH", "5-wave geopotential height [gpm]"},
      /* 223 */ {"CNWAT", "Plant canopy surface water [kg/m^2]"},
      /* 224 */ {"SOTYP", "Soil type (Zobler) [0..9]"},
      /* 225 */ {"VGTYP", "Vegetation type (as in SiB) [0..13]"},
      /* 226 */ {"BMIXL", "Blackadar's mixing length scale [m]"},
      /* 227 */ {"AMIXL", "Asymptotic mixing length scale [m]"},
      /* 228 */ {"PEVAP", "Pot. evaporation [kg/m^2]"},
      /* 229 */ {"SNOHF", "Snow phase-change heat flux [W/m^2]"},
      /* 230 */ {"5WAVA", "5-wave geopot. height anomaly [gpm]"},
      /* 231 */ {"MFLUX", "Convective cloud mass flux [Pa/s]"},
      /* 232 */ {"DTRF", "Downward total radiation flux [W/m^2]"},
      /* 233 */ {"UTRF", "Upward total radiation flux [W/m^2]"},
      /* 234 */ {"BGRUN", "Baseflow-groundwater runoff [kg/m^2]"},
      /* 235 */ {"SSRUN", "Storm surface runoff [kg/m^2]"},
      /* 236 */ {"SIPD", "Supercooled large droplet (SLD) icing pot. diagn. []"},
      /* 237 */ {"O3TOT", "Total ozone [kg/m^2]"},
      /* 238 */ {"SNOWC", "Snow cover [%]"},
      /* 239 */ {"SNOT", "Snow temp. [K]"},
      /* 240 */ {"COVTW", "Covariance T and w [K*m/s]"},
      /* 241 */ {"LRGHR", "Large scale condensation heating [K/s]"},
      /* 242 */ {"CNVHR", "Deep convective heating [K/s]"},
      /* 243 */ {"CNVMR", "Deep convective moistening [kg/kg/s]"},
      /* 244 */ {"SHAHR", "Shallow convective heating [K/s]"},
      /* 245 */ {"SHAMR", "Shallow convective moistening [kg/kg/s]"},
      /* 246 */ {"VDFHR", "Vertical diffusion heating [K/s]"},
      /* 247 */ {"VDFUA", "Vertical diffusion zonal accel [m/s^2]"},
      /* 248 */ {"VDFVA", "Vertical diffusion meridional accel [m/s^2]"},
      /* 249 */ {"VDFMR", "Vertical diffusion moistening [kg/kg/s]"},
      /* 250 */ {"SWHR", "Solar radiative heating [K/s]"},
      /* 251 */ {"LWHR", "Longwave radiative heating [K/s]"},
      /* 252 */ {"CD", "Drag coefficient [non-dim]"},
      /* 253 */ {"FRICV", "Friction velocity [m/s]"},
      /* 254 */ {"RI", "Richardson number [non-dim]"},
      /* 255 */ {"var255", "undefined"},
};

/*
 * parameter table for the NCEP/NCAR Reanalysis Project
 * center = 7, subcenter = 0/2, parameter table = 1/2
 * in a SNAFU the operational and reanalysis tables diverged
 * and both retained the same parameter table numbers (1,2)
 *
 * some of the Reanalysis files have subcenter=2 while others
 * use subcenter=0  (subcenter field is not standard (7/97))
 *
 * Some ways to tell Reanalysis files from OPN files
 *  Reanalysis: always generated by process 80 - T62 28 level model
 * Original subcenter=0 Reanalysis files had 
 *  2.5x2.5 (144x73) lat-long grid or 192x94 Gaussian grid (PDS grid=255?)
 */

struct ParmTable parm_table_ncep_reanal[256] = {
   /* 0 */ {"var0", "undefined"},
   /* 1 */ {"PRES", "Pressure [Pa]"},
   /* 2 */ {"PRMSL", "Pressure reduced to MSL [Pa]"},
   /* 3 */ {"PTEND", "Pressure tendency [Pa/s]"},
   /* 4 */ {"var4", "undefined"},
   /* 5 */ {"var5", "undefined"},
   /* 6 */ {"GP", "Geopotential [m^2/s^2]"},
   /* 7 */ {"HGT", "Geopotential height [gpm]"},
   /* 8 */ {"DIST", "Geometric height [m]"},
   /* 9 */ {"HSTDV", "Std dev of height [m]"},
   /* 10 */ {"HVAR", "Variance of height [m^2]"},
   /* 11 */ {"TMP", "Temp. [K]"},
   /* 12 */ {"VTMP", "Virtual temp. [K]"},
   /* 13 */ {"POT", "Potential temp. [K]"},
   /* 14 */ {"EPOT", "Pseudo-adiabatic pot. temp. [K]"},
   /* 15 */ {"TMAX", "Max. temp. [K]"},
   /* 16 */ {"TMIN", "Min. temp. [K]"},
   /* 17 */ {"DPT", "Dew point temp. [K]"},
   /* 18 */ {"DEPR", "Dew point depression [K]"},
   /* 19 */ {"LAPR", "Lapse rate [K/m]"},
   /* 20 */ {"VISIB", "Visibility [m]"},
   /* 21 */ {"RDSP1", "Radar spectra (1) [non-dim]"},
   /* 22 */ {"RDSP2", "Radar spectra (2) [non-dim]"},
   /* 23 */ {"RDSP3", "Radar spectra (3) [non-dim]"},
   /* 24 */ {"var24", "undefined"},
   /* 25 */ {"TMPA", "Temp. anomaly [K]"},
   /* 26 */ {"PRESA", "Pressure anomaly [Pa]"},
   /* 27 */ {"GPA", "Geopotential height anomaly [gpm]"},
   /* 28 */ {"WVSP1", "Wave spectra (1) [non-dim]"},
   /* 29 */ {"WVSP2", "Wave spectra (2) [non-dim]"},
   /* 30 */ {"WVSP3", "Wave spectra (3) [non-dim]"},
   /* 31 */ {"WDIR", "Wind direction [deg]"},
   /* 32 */ {"WIND", "Wind speed [m/s]"},
   /* 33 */ {"UGRD", "u wind [m/s]"},
   /* 34 */ {"VGRD", "v wind [m/s]"},
   /* 35 */ {"STRM", "Stream function [m^2/s]"},
   /* 36 */ {"VPOT", "Velocity potential [m^2/s]"},
   /* 37 */ {"MNTSF", "Montgomery stream function [m^2/s^2]"},
   /* 38 */ {"SGCVV", "Sigma coord. vertical velocity [/s]"},
   /* 39 */ {"VVEL", "Pressure vertical velocity [Pa/s]"},
   /* 40 */ {"DZDT", "Geometric vertical velocity [m/s]"},
   /* 41 */ {"ABSV", "Absolute vorticity [/s]"},
   /* 42 */ {"ABSD", "Absolute divergence [/s]"},
   /* 43 */ {"RELV", "Relative vorticity [/s]"},
   /* 44 */ {"RELD", "Relative divergence [/s]"},
   /* 45 */ {"VUCSH", "Vertical u shear [/s]"},
   /* 46 */ {"VVCSH", "Vertical v shear [/s]"},
   /* 47 */ {"DIRC", "Direction of current [deg]"},
   /* 48 */ {"SPC", "Speed of current [m/s]"},
   /* 49 */ {"UOGRD", "u of current [m/s]"},
   /* 50 */ {"VOGRD", "v of current [m/s]"},
   /* 51 */ {"SPFH", "Specific humidity [kg/kg]"},
   /* 52 */ {"RH", "Relative humidity [%]"},
   /* 53 */ {"MIXR", "Humidity mixing ratio [kg/kg]"},
   /* 54 */ {"PWAT", "Precipitable water [kg/m^2]"},
   /* 55 */ {"VAPP", "Vapor pressure [Pa]"},
   /* 56 */ {"SATD", "Saturation deficit [Pa]"},
   /* 57 */ {"EVP", "Evaporation [kg/m^2]"},
   /* 58 */ {"CICE", "Cloud Ice [kg/kg]"},
   /* 59 */ {"PRATE", "Precipitation rate [kg/m^2/s]"},
   /* 60 */ {"TSTM", "Thunderstorm probability [%]"},
   /* 61 */ {"APCP", "Total precipitation [kg/m^2]"},
   /* 62 */ {"NCPCP", "Large scale precipitation [kg/m^2]"},
   /* 63 */ {"ACPCP", "Convective precipitation [kg/m^2]"},
   /* 64 */ {"SRWEQ", "Snowfall rate water equiv. [kg/m^2/s]"},
   /* 65 */ {"WEASD", "Accum. snow [kg/m^2]"},
   /* 66 */ {"SNOD", "Snow depth [m]"},
   /* 67 */ {"MIXHT", "Mixed layer depth [m]"},
   /* 68 */ {"TTHDP", "Transient thermocline depth [m]"},
   /* 69 */ {"MTHD", "Main thermocline depth [m]"},
   /* 70 */ {"MTHA", "Main thermocline anomaly [m]"},
   /* 71 */ {"TCDC", "Total cloud cover [%]"},
   /* 72 */ {"CDCON", "Convective cloud cover [%]"},
   /* 73 */ {"LCDC", "Low level cloud cover [%]"},
   /* 74 */ {"MCDC", "Mid level cloud cover [%]"},
   /* 75 */ {"HCDC", "High level cloud cover [%]"},
   /* 76 */ {"CWAT", "Cloud water [kg/m^2]"},
   /* 77 */ {"var77", "undefined"},
   /* 78 */ {"SNOC", "Convective snow [kg/m^2]"},
   /* 79 */ {"SNOL", "Large scale snow [kg/m^2]"},
   /* 80 */ {"WTMP", "Water temp. [K]"},
   /* 81 */ {"LAND", "Land-sea mask [1=land; 0=sea]"},
   /* 82 */ {"DSLM", "Deviation of sea level from mean [m]"},
   /* 83 */ {"SFCR", "Surface roughness [m]"},
   /* 84 */ {"ALBDO", "Albedo [%]"},
   /* 85 */ {"TSOIL", "Soil temp. [K]"},
   /* 86 */ {"SOILM", "Soil moisture content [kg/m^2]"},
   /* 87 */ {"VEG", "Vegetation [%]"},
   /* 88 */ {"SALTY", "Salinity [kg/kg]"},
   /* 89 */ {"DEN", "Density [kg/m^3]"},
   /* 90 */ {"RUNOF", "Runoff [kg/m^2]"},
   /* 91 */ {"ICEC", "Ice concentration [ice=1;no ice=0]"},
   /* 92 */ {"ICETK", "Ice thickness [m]"},
   /* 93 */ {"DICED", "Direction of ice drift [deg]"},
   /* 94 */ {"SICED", "Speed of ice drift [m/s]"},
   /* 95 */ {"UICE", "u of ice drift [m/s]"},
   /* 96 */ {"VICE", "v of ice drift [m/s]"},
   /* 97 */ {"ICEG", "Ice growth rate [m/s]"},
   /* 98 */ {"ICED", "Ice divergence [/s]"},
   /* 99 */ {"SNOM", "Snow melt [kg/m^2]"},
   /* 100 */ {"HTSGW", "Sig height of wind waves and swell [m]"},
   /* 101 */ {"WVDIR", "Direction of wind waves [deg]"},
   /* 102 */ {"WVHGT", "Sig height of wind waves [m]"},
   /* 103 */ {"WVPER", "Mean period of wind waves [s]"},
   /* 104 */ {"SWDIR", "Direction of swell waves [deg]"},
   /* 105 */ {"SWELL", "Sig height of swell waves [m]"},
   /* 106 */ {"SWPER", "Mean period of swell waves [s]"},
   /* 107 */ {"DIRPW", "Primary wave direction [deg]"},
   /* 108 */ {"PERPW", "Primary wave mean period [s]"},
   /* 109 */ {"DIRSW", "Secondary wave direction [deg]"},
   /* 110 */ {"PERSW", "Secondary wave mean period [s]"},
   /* 111 */ {"NSWRS", "Net short wave (surface) [W/m^2]"},
   /* 112 */ {"NLWRS", "Net long wave (surface) [W/m^2]"},
   /* 113 */ {"NSWRT", "Net short wave (top) [W/m^2]"},
   /* 114 */ {"NLWRT", "Net long wave (top) [W/m^2]"},
   /* 115 */ {"LWAVR", "Long wave [W/m^2]"},
   /* 116 */ {"SWAVR", "Short wave [W/m^2]"},
   /* 117 */ {"GRAD", "Global radiation [W/m^2]"},
   /* 118 */ {"var118", "undefined"},
   /* 119 */ {"var119", "undefined"},
   /* 120 */ {"var120", "undefined"},
   /* 121 */ {"LHTFL", "Latent heat flux [W/m^2]"},
   /* 122 */ {"SHTFL", "Sensible heat flux [W/m^2]"},
   /* 123 */ {"BLYDP", "Boundary layer dissipation [W/m^2]"},
   /* 124 */ {"UFLX", "Zonal momentum flux [N/m^2]"},
   /* 125 */ {"VFLX", "Meridional momentum flux [N/m^2]"},
   /* 126 */ {"WMIXE", "Wind mixing energy [J]"},
   /* 127 */ {"IMGD", "Image data [integer]"},
   /* 128 */ {"MSLSA", "Mean sea level pressure (Std Atm) [Pa]"},
   /* 129 */ {"MSLMA", "Mean sea level pressure (MAPS) [Pa]"},
   /* 130 */ {"MSLET", "Mean sea level pressure (ETA model) [Pa]"},
   /* 131 */ {"LFTX", "Surface lifted index [K]"},
   /* 132 */ {"4LFTX", "Best (4-layer) lifted index [K]"},
   /* 133 */ {"KX", "K index [K]"},
   /* 134 */ {"SX", "Sweat index [K]"},
   /* 135 */ {"MCONV", "Horizontal moisture divergence [kg/kg/s]"},
   /* 136 */ {"VSSH", "Vertical speed shear [1/s]"},
   /* 137 */ {"TSLSA", "3-hr pressure tendency [Pa/s]"},
   /* 138 */ {"BVF2", "Brunt-Vaisala frequency^2 [1/s^2]"},
   /* 139 */ {"PVMW", "Potential vorticity (mass-weighted) [1/s/m]"},
   /* 140 */ {"CRAIN", "Categorical rain [yes=1;no=0]"},
   /* 141 */ {"CFRZR", "Categorical freezing rain [yes=1;no=0]"},
   /* 142 */ {"CICEP", "Categorical ice pellets [yes=1;no=0]"},
   /* 143 */ {"CSNOW", "Categorical snow [yes=1;no=0]"},
   /* 144 */ {"SOILW", "Volumetric soil moisture [fraction]"},
   /* 145 */ {"PEVPR", "Potential evaporation rate [W/m^2]"},
   /* 146 */ {"CWORK", "Cloud work function [J/kg]"},
   /* 147 */ {"U-GWD", "Zonal gravity wave stress [N/m^2]"},
   /* 148 */ {"V-GWD", "Meridional gravity wave stress [N/m^2]"},
   /* 149 */ {"PV___", "Potential vorticity [m^2/s/kg]"},
   /* 150 */ {"var150", "undefined"},
   /* 151 */ {"var151", "undefined"},
   /* 152 */ {"var152", "undefined"},
   /* 153 */ {"MFXDV", "Moisture flux divergence [gr/gr*m/s/m]"},
   /* 154 */ {"var154", "undefined"},
   /* 155 */ {"GFLUX", "Ground heat flux [W/m^2]"},
   /* 156 */ {"CIN", "Convective inhibition [J/kg]"},
   /* 157 */ {"CAPE", "Convective Avail. Pot. Energy [J/kg]"},
   /* 158 */ {"TKE", "Turbulent kinetic energy [J/kg]"},
   /* 159 */ {"CONDP", "Lifted parcel condensation pressure [Pa]"},
   /* 160 */ {"CSUSF", "Clear sky upward solar flux [W/m^2]"},
   /* 161 */ {"CSDSF", "Clear sky downward solar flux [W/m^2]"},
   /* 162 */ {"CSULF", "Clear sky upward long wave flux [W/m^2]"},
   /* 163 */ {"CSDLF", "Clear sky downward long wave flux [W/m^2]"},
   /* 164 */ {"CFNSF", "Cloud forcing net solar flux [W/m^2]"},
   /* 165 */ {"CFNLF", "Cloud forcing net long wave flux [W/m^2]"},
   /* 166 */ {"VBDSF", "Visible beam downward solar flux [W/m^2]"},
   /* 167 */ {"VDDSF", "Visible diffuse downward solar flux [W/m^2]"},
   /* 168 */ {"NBDSF", "Near IR beam downward solar flux [W/m^2]"},
   /* 169 */ {"NDDSF", "Near IR diffuse downward solar flux [W/m^2]"},
   /* 170 */ {"USTR", "U wind stress [N/m^2]"},
   /* 171 */ {"VSTR", "V wind stress [N/m^2]"},
   /* 172 */ {"MFLX", "Momentum flux [N/m^2]"},
   /* 173 */ {"LMH", "Mass point model surface [integer]"},
   /* 174 */ {"LMV", "Velocity point model surface [integer]"},
   /* 175 */ {"SGLYR", "Nearby model level [integer]"},
   /* 176 */ {"NLAT", "Latitude [deg]"},
   /* 177 */ {"ELON", "Longitude [deg]"},
   /* 178 */ {"UMAS", "Mass weighted u [gm/m*K*s]"},
   /* 179 */ {"VMAS", "Mass weighted v [gm/m*K*s]"},
   /* 180 */ {"XPRATE", "corrected precip [kg/m^2/s]"},
   /* 181 */ {"LPSX", "x-gradient of log pressure [1/m]"},
   /* 182 */ {"LPSY", "y-gradient of log pressure [1/m]"},
   /* 183 */ {"HGTX", "x-gradient of height [m/m]"},
   /* 184 */ {"HGTY", "y-gradient of height [m/m]"},
   /* 185 */ {"STDZ", "Std dev of Geop. hgt. [m]"},
   /* 186 */ {"STDU", "Std dev of zonal wind [m/s]"},
   /* 187 */ {"STDV", "Std dev of meridional wind [m/s]"},
   /* 188 */ {"STDQ", "Std dev of spec. hum. [gm/gm]"},
   /* 189 */ {"STDT", "Std dev of temp. [K]"},
   /* 190 */ {"CBUW", "Covar. u and omega [m/s*Pa/s]"},
   /* 191 */ {"CBVW", "Covar. v and omega [m/s*Pa/s]"},
   /* 192 */ {"CBUQ", "Covar. u and specific hum [m/s*gm/gm]"},
   /* 193 */ {"CBVQ", "Covar. v and specific hum [m/s*gm/gm]"},
   /* 194 */ {"CBTW", "Covar. T and omega [K*Pa/s]"},
   /* 195 */ {"CBQW", "Covar. spec. hum and omega [gm/gm*Pa/s]"},
   /* 196 */ {"CBMZW", "Covar. v and u [m^2/s^2]"},
   /* 197 */ {"CBTZW", "Covar. u and T [K*m/s]"},
   /* 198 */ {"CBTMW", "Covar. v and T [K*m/s]"},
   /* 199 */ {"STDRH", "Std dev of Rel. Hum. [%]"},
   /* 200 */ {"SDTZ", "Std dev of time tend of geop. hgt [m]"},
   /* 201 */ {"ICWAT", "Ice-free water surface [%]"},
   /* 202 */ {"SDTU", "Std dev of time tend of zonal wind [m/s]"},
   /* 203 */ {"SDTV", "Std dev of time tend of merid wind [m/s]"},
   /* 204 */ {"DSWRF", "Downward solar radiation flux [W/m^2]"},
   /* 205 */ {"DLWRF", "Downward long wave flux [W/m^2]"},
   /* 206 */ {"SDTQ", "Std dev of time tend of spec. hum [gm/gm]"},
   /* 207 */ {"MSTAV", "Moisture availability [%]"},
   /* 208 */ {"SFEXC", "Exchange coefficient [kg*m/m^3/s]"},
   /* 209 */ {"MIXLY", "No. of mixed layers next to sfc [integer]"},
   /* 210 */ {"SDTT", "Std dev of time tend of temp. [K]"},
   /* 211 */ {"USWRF", "Upward solar radiation flux [W/m^2]"},
   /* 212 */ {"ULWRF", "Upward long wave flux [W/m^2]"},
   /* 213 */ {"CDLYR", "Non-convective cloud [%]"},
   /* 214 */ {"CPRAT", "Convective precip. rate [kg/m^2/s]"},
   /* 215 */ {"TTDIA", "Temp. tendency by all physics [K/s]"},
   /* 216 */ {"TTRAD", "Temp. tendency by all radiation [K/s]"},
   /* 217 */ {"TTPHY", "Temp. tendency by nonrad physics [K/s]"},
   /* 218 */ {"PREIX", "Precipitation index [fraction]"},
   /* 219 */ {"TSD1D", "Std dev of IR T over 1x1 deg area [K]"},
   /* 220 */ {"NLSGP", "Natural log of surface pressure [ln(kPa)]"},
   /* 221 */ {"SDTRH", "Std dev of time tend of rel hum [%]"},
   /* 222 */ {"5WAVH", "5-wave geopotential height [gpm]"},
   /* 223 */ {"CNWAT", "Plant canopy surface water [kg/m^2]"},
   /* 224 */ {"PLTRS", "Max. stomato plant resistance [s/m]"},
   /* 225 */ {"RHCLD", "RH-type cloud cover [%]"},
   /* 226 */ {"BMIXL", "Blackadar's mixing length scale [m]"},
   /* 227 */ {"AMIXL", "Asymptotic mixing length scale [m]"},
   /* 228 */ {"PEVAP", "Pot. evaporation [kg/m^2]"},
   /* 229 */ {"SNOHF", "Snow melt heat flux [W/m^2]"},
   /* 230 */ {"SNOEV", "Snow sublimation heat flux [W/m^2]"},
   /* 231 */ {"MFLUX", "Convective cloud mass flux [Pa/s]"},
   /* 232 */ {"DTRF", "Downward total radiation flux [W/m^2]"},
   /* 233 */ {"UTRF", "Upward total radiation flux [W/m^2]"},
   /* 234 */ {"BGRUN", "Baseflow-groundwater runoff [kg/m^2]"},
   /* 235 */ {"SSRUN", "Storm surface runoff [kg/m^2]"},
   /* 236 */ {"var236", "undefined"},
   /* 237 */ {"OZONE", "Total column ozone [Dobson]"},
   /* 238 */ {"SNOWC", "Snow cover [%]"},
   /* 239 */ {"SNOT", "Snow temp. [K]"},
   /* 240 */ {"GLCR", "Permanent snow points [mask]"},
   /* 241 */ {"LRGHR", "Large scale condensation heating [K/s]"},
   /* 242 */ {"CNVHR", "Deep convective heating [K/s]"},
   /* 243 */ {"CNVMR", "Deep convective moistening [kg/kg/s]"},
   /* 244 */ {"SHAHR", "Shallow convective heating [K/s]"},
   /* 245 */ {"SHAMR", "Shallow convective moistening [kg/kg/s]"},
   /* 246 */ {"VDFHR", "Vertical diffusion heating [K/s]"},
   /* 247 */ {"VDFUA", "Vertical diffusion zonal accel [m/s^2]"},
   /* 248 */ {"VDFVA", "Vertical diffusion meridional accel [m/s^2]"},
   /* 249 */ {"VDFMR", "Vertical diffusion moistening [kg/kg/s]"},
   /* 250 */ {"SWHR", "Solar radiative heating [K/s]"},
   /* 251 */ {"LWHR", "Longwave radiative heating [K/s]"},
   /* 252 */ {"CD", "Drag coefficient [non-dim]"},
   /* 253 */ {"FRICV", "Friction velocity [m/s]"},
   /* 254 */ {"RI", "Richardson number [non-dim]"},
   /* 255 */ {"var255", "undefined"},
};

struct ParmTable parm_table_nceptab_131[256] = {
      /* 0 */ {"var0", "undefined"},
      /* 1 */ {"PRES", "Pressure [Pa]"},
      /* 2 */ {"PRMSL", "Mean sea level pressure (Shuell method) [Pa]"},
      /* 3 */ {"PTEND", "Pressure tendency [Pa/s]"},
      /* 4 */ {"PVORT", "Pot. vorticity [km^2/kg/s]"},
      /* 5 */ {"ICAHT", "ICAO Standard Atmosphere Reference Height [M]"},
      /* 6 */ {"GP", "Geopotential [m^2/s^2]"},
      /* 7 */ {"HGT", "Geopotential height [gpm]"},
      /* 8 */ {"DIST", "Geometric height [m]"},
      /* 9 */ {"HSTDV", "Std dev of height [m]"},
      /* 10 */ {"TOZNE", "Total ozone [Dobson]"},
      /* 11 */ {"TMP", "Temp. [K]"},
      /* 12 */ {"VTMP", "Virtual temp. [K]"},
      /* 13 */ {"POT", "Potential temp. [K]"},
      /* 14 */ {"EPOT", "Pseudo-adiabatic pot. temp. [K]"},
      /* 15 */ {"TMAX", "Max. temp. [K]"},
      /* 16 */ {"TMIN", "Min. temp. [K]"},
      /* 17 */ {"DPT", "Dew point temp. [K]"},
      /* 18 */ {"DEPR", "Dew point depression [K]"},
      /* 19 */ {"LAPR", "Lapse rate [K/m]"},
      /* 20 */ {"VIS", "Visibility [m]"},
      /* 21 */ {"RDSP1", "Radar spectra (1) [non-dim]"},
      /* 22 */ {"RDSP2", "Radar spectra (2) [non-dim]"},
      /* 23 */ {"RDSP3", "Radar spectra (3) [non-dim]"},
      /* 24 */ {"PLI", "Parcel lifted index (to 500 hPa) [K]"},
      /* 25 */ {"TMPA", "Temp. anomaly [K]"},
      /* 26 */ {"PRESA", "Pressure anomaly [Pa]"},
      /* 27 */ {"GPA", "Geopotential height anomaly [gpm]"},
      /* 28 */ {"WVSP1", "Wave spectra (1) [non-dim]"},
      /* 29 */ {"WVSP2", "Wave spectra (2) [non-dim]"},
      /* 30 */ {"WVSP3", "Wave spectra (3) [non-dim]"},
      /* 31 */ {"WDIR", "Wind direction [deg]"},
      /* 32 */ {"WIND", "Wind speed [m/s]"},
      /* 33 */ {"UGRD", "u wind [m/s]"},
      /* 34 */ {"VGRD", "v wind [m/s]"},
      /* 35 */ {"STRM", "Stream function [m^2/s]"},
      /* 36 */ {"VPOT", "Velocity potential [m^2/s]"},
      /* 37 */ {"MNTSF", "Montgomery stream function [m^2/s^2]"},
      /* 38 */ {"SGCVV", "Sigma coord. vertical velocity [/s]"},
      /* 39 */ {"VVEL", "Pressure vertical velocity [Pa/s]"},
      /* 40 */ {"DZDT", "Geometric vertical velocity [m/s]"},
      /* 41 */ {"ABSV", "Absolute vorticity [/s]"},
      /* 42 */ {"ABSD", "Absolute divergence [/s]"},
      /* 43 */ {"RELV", "Relative vorticity [/s]"},
      /* 44 */ {"RELD", "Relative divergence [/s]"},
      /* 45 */ {"VUCSH", "Vertical u shear [/s]"},
      /* 46 */ {"VVCSH", "Vertical v shear [/s]"},
      /* 47 */ {"DIRC", "Direction of current [deg]"},
      /* 48 */ {"SPC", "Speed of current [m/s]"},
      /* 49 */ {"UOGRD", "u of current [m/s]"},
      /* 50 */ {"VOGRD", "v of current [m/s]"},
      /* 51 */ {"SPFH", "Specific humidity [kg/kg]"},
      /* 52 */ {"RH", "Relative humidity [%]"},
      /* 53 */ {"MIXR", "Humidity mixing ratio [kg/kg]"},
      /* 54 */ {"PWAT", "Precipitable water [kg/m^2]"},
      /* 55 */ {"VAPP", "Vapor pressure [Pa]"},
      /* 56 */ {"SATD", "Saturation deficit [Pa]"},
      /* 57 */ {"EVP", "Evaporation [kg/m^2]"},
      /* 58 */ {"CICE", "Cloud Ice [kg/m^2]"},
      /* 59 */ {"PRATE", "Precipitation rate [kg/m^2/s]"},
      /* 60 */ {"TSTM", "Thunderstorm probability [%]"},
      /* 61 */ {"APCP", "Total precipitation [kg/m^2]"},
      /* 62 */ {"NCPCP", "Large scale precipitation [kg/m^2]"},
      /* 63 */ {"ACPCP", "Convective precipitation [kg/m^2]"},
      /* 64 */ {"SRWEQ", "Snowfall rate water equiv. [kg/m^2/s]"},
      /* 65 */ {"WEASD", "Accum. snow [kg/m^2]"},
      /* 66 */ {"SNOD", "Snow depth [m]"},
      /* 67 */ {"MIXHT", "Mixed layer depth [m]"},
      /* 68 */ {"TTHDP", "Transient thermocline depth [m]"},
      /* 69 */ {"MTHD", "Main thermocline depth [m]"},
      /* 70 */ {"MTHA", "Main thermocline anomaly [m]"},
      /* 71 */ {"TCDC", "Total cloud cover [%]"},
      /* 72 */ {"CDCON", "Convective cloud cover [%]"},
      /* 73 */ {"LCDC", "Low level cloud cover [%]"},
      /* 74 */ {"MCDC", "Mid level cloud cover [%]"},
      /* 75 */ {"HCDC", "High level cloud cover [%]"},
      /* 76 */ {"CWAT", "Cloud water [kg/m^2]"},
      /* 77 */ {"BLI", "Best lifted index (to 500 hPa) [K]"},
      /* 78 */ {"SNOC", "Convective snow [kg/m^2]"},
      /* 79 */ {"SNOL", "Large scale snow [kg/m^2]"},
      /* 80 */ {"WTMP", "Water temp. [K]"},
      /* 81 */ {"LAND", "Land cover (land=1;sea=0) [fraction]"},
      /* 82 */ {"DSLM", "Deviation of sea level from mean [m]"},
      /* 83 */ {"SFCR", "Surface roughness [m]"},
      /* 84 */ {"ALBDO", "Albedo [%]"},
      /* 85 */ {"TSOIL", "Soil temp. [K]"},
      /* 86 */ {"SOILM", "Soil moisture content [kg/m^2]"},
      /* 87 */ {"VEG", "Vegetation [%]"},
      /* 88 */ {"SALTY", "Salinity [kg/kg]"},
      /* 89 */ {"DEN", "Density [kg/m^3]"},
      /* 90 */ {"WATR", "Water runoff [kg/m^2]"},
      /* 91 */ {"ICEC", "Ice concentration (ice=1;no ice=0) [fraction]"},
      /* 92 */ {"ICETK", "Ice thickness [m]"},
      /* 93 */ {"DICED", "Direction of ice drift [deg]"},
      /* 94 */ {"SICED", "Speed of ice drift [m/s]"},
      /* 95 */ {"UICE", "u of ice drift [m/s]"},
      /* 96 */ {"VICE", "v of ice drift [m/s]"},
      /* 97 */ {"ICEG", "Ice growth rate [m/s]"},
      /* 98 */ {"ICED", "Ice divergence [/s]"},
      /* 99 */ {"SNOM", "Snow melt [kg/m^2]"},
      /* 100 */ {"HTSGW", "Sig height of wind waves and swell [m]"},
      /* 101 */ {"WVDIR", "Direction of wind waves [deg]"},
      /* 102 */ {"WVHGT", "Sig height of wind waves [m]"},
      /* 103 */ {"WVPER", "Mean period of wind waves [s]"},
      /* 104 */ {"SWDIR", "Direction of swell waves [deg]"},
      /* 105 */ {"SWELL", "Sig height of swell waves [m]"},
      /* 106 */ {"SWPER", "Mean period of swell waves [s]"},
      /* 107 */ {"DIRPW", "Primary wave direction [deg]"},
      /* 108 */ {"PERPW", "Primary wave mean period [s]"},
      /* 109 */ {"DIRSW", "Secondary wave direction [deg]"},
      /* 110 */ {"PERSW", "Secondary wave mean period [s]"},
      /* 111 */ {"NSWRS", "Net short wave (surface) [W/m^2]"},
      /* 112 */ {"NLWRS", "Net long wave (surface) [W/m^2]"},
      /* 113 */ {"NSWRT", "Net short wave (top) [W/m^2]"},
      /* 114 */ {"NLWRT", "Net long wave (top) [W/m^2]"},
      /* 115 */ {"LWAVR", "Long wave [W/m^2]"},
      /* 116 */ {"SWAVR", "Short wave [W/m^2]"},
      /* 117 */ {"GRAD", "Global radiation [W/m^2]"},
      /* 118 */ {"BRTMP", "Brightness temperature [K]"},
      /* 119 */ {"LWRAD", "Radiance with respect to wave no. [W/m/sr]"},
      /* 120 */ {"SWRAD", "Radiance with respect ot wave len. [W/m^3/sr]"},
      /* 121 */ {"LHTFL", "Latent heat flux [W/m^2]"},
      /* 122 */ {"SHTFL", "Sensible heat flux [W/m^2]"},
      /* 123 */ {"BLYDP", "Boundary layer dissipation [W/m^2]"},
      /* 124 */ {"UFLX", "Zonal momentum flux [N/m^2]"},
      /* 125 */ {"VFLX", "Meridional momentum flux [N/m^2]"},
      /* 126 */ {"WMIXE", "Wind mixing energy [J]"},
      /* 127 */ {"IMGD", "Image data []"},
      /* 128 */ {"MSLSA", "Mean sea level pressure (Std Atm) [Pa]"},
      /* 129 */ {"var129", "undefined"},
      /* 130 */ {"MSLET", "Mean sea level pressure (Mesinger method) [Pa]"},
      /* 131 */ {"LFTX", "Surface lifted index [K]"},
      /* 132 */ {"4LFTX", "Best (4-layer) lifted index [K]"},
      /* 133 */ {"var133", "undefined"},
      /* 134 */ {"PRESN", "Pressure (nearest grid point) [Pa]"},
      /* 135 */ {"MCONV", "Horizontal moisture divergence [kg/kg/s]"},
      /* 136 */ {"VWSH", "Vertical speed shear [1/s]"},
      /* 137 */ {"var137", "undefined"},
      /* 138 */ {"var138", "undefined"},
      /* 139 */ {"PVMW", "Potential vorticity (mass-weighted) [1/s/m]"},
      /* 140 */ {"CRAIN", "Categorical rain [yes=1;no=0]"},
      /* 141 */ {"CFRZR", "Categorical freezing rain [yes=1;no=0]"},
      /* 142 */ {"CICEP", "Categorical ice pellets [yes=1;no=0]"},
      /* 143 */ {"CSNOW", "Categorical snow [yes=1;no=0]"},
      /* 144 */ {"SOILW", "Volumetric soil moisture (frozen + liquid) [fraction]"},
      /* 145 */ {"PEVPR", "Potential evaporation rate [W/m^2]"},
      /* 146 */ {"VEGT", "Vegetation canopy temperature [K]"},
      /* 147 */ {"BARET", "Bare soil surface skin temperature [K]"},
      /* 148 */ {"AVSFT", "Average surface skin temperature [K]"},
      /* 149 */ {"RADT", "Effective radiative skin temperature [K]"},
      /* 150 */ {"SSTOR", "Surface water storage [kg/m^2]"},
      /* 151 */ {"LSOIL", "Liquid soil moisture content (non-frozen) [kg/m^2]"},
      /* 152 */ {"EWATR", "Open water evaporation (standing water) [W/m^2]"},
      /* 153 */ {"CLWMR", "Cloud water [kg/kg]"},
      /* 154 */ {"var154", "undefined"},
      /* 155 */ {"GFLUX", "Ground Heat Flux [W/m^2]"},
      /* 156 */ {"CIN", "Convective inhibition [J/kg]"},
      /* 157 */ {"CAPE", "Convective available potential energy [J/kg]"},
      /* 158 */ {"TKE", "Turbulent Kinetic Energy [J/kg]"},
      /* 159 */ {"MXSALB", "Maximum snow albedo [%]"},
      /* 160 */ {"SOILL", "Liquid volumetric soil moisture (non-frozen) [fraction]"},
      /* 161 */ {"ASNOW", "Frozen precipitation (e.g. snowfall) [kg/m^2]"},
      /* 162 */ {"ARAIN", "Liquid precipitation (rainfall) [kg/m^2]"},
      /* 163 */ {"GWREC", "Groundwater recharge [kg/m^2]"},
      /* 164 */ {"QREC", "Flood plain recharge [kg/m^2]"},
      /* 165 */ {"SNOWT", "Snow temperature, depth-avg [K]"},
      /* 166 */ {"VBDSF", "Visible beam downward solar flux [W/m^2]"},
      /* 167 */ {"VDDSF", "Visible diffuse downward solar flux [W/m^2]"},
      /* 168 */ {"NBDSF", "Near IR beam downward solar flux [W/m^2]"},
      /* 169 */ {"NDDSF", "Near IR diffuse downward solar flux [W/m^2]"},
      /* 170 */ {"SNFALB", "Snow-free albedo [%]"},
      /* 171 */ {"RLYRS", "Number of soil layers in root zone [non-dim]"},
      /* 172 */ {"FLX", "Momentum flux N/m2 [M]"},
      /* 173 */ {"LMH", "Mass point model surface [non-dim]"},
      /* 174 */ {"LMV", "Velocity point model surface [non-dim]"},
      /* 175 */ {"MLYNO", "Model layer number (from bottom up) [non-dim]"},
      /* 176 */ {"NLAT", "Latitude (-90 to +90) [deg]"},
      /* 177 */ {"ELON", "East longitude (0-360) [deg]"},
      /* 178 */ {"ICMR", "Ice mixing ratio [kg/kg]"},
      /* 179 */ {"ACOND", "Aerodynamic conductance [m/s]"},
      /* 180 */ {"SNOAG", "Snow age [s]"},
      /* 181 */ {"CCOND", "Canopy conductance [m/s]"},
      /* 182 */ {"LAI", "Leaf area index (0-9) [non-dim]"},
      /* 183 */ {"SFCRH", "Roughness length for heat [m]"},
      /* 184 */ {"SALBD", "Snow albedo (over snow cover area only) [%]"},
      /* 185 */ {"var185", "undefined"},
      /* 186 */ {"var186", "undefined"},
      /* 187 */ {"NDVI", "Normalized Difference Vegetation Index []"},
      /* 188 */ {"DRIP", "Rate of water dropping from canopy to gnd [kg/m^2]"},
      /* 189 */ {"LANDN", "Land cover (nearest neighbor) [sea=0,land=1]"},
      /* 190 */ {"HLCY", "Storm relative helicity [m^2/s^2]"},
      /* 191 */ {"NLATN", "Latitude (nearest neigbhbor) (-90 to +90) [deg]"},
      /* 192 */ {"ELONN", "East longitude (nearest neigbhbor) (0-360) [deg]"},
      /* 193 */ {"var193", "undefined"},
      /* 194 */ {"CPOFP", "Prob. of frozen precipitation [%]"},
      /* 195 */ {"var195", "undefined"},
      /* 196 */ {"USTM", "u-component of storm motion [m/s]"},
      /* 197 */ {"VSTM", "v-component of storm motion [m/s]"},
      /* 198 */ {"SBSNO", "Sublimation (evaporation from snow) [W/m^2]"},
      /* 199 */ {"EVBS", "Direct evaporation from bare soil [W/m^2]"},
      /* 200 */ {"EVCW", "Canopy water evaporation [W/m^2]"},
      /* 201 */ {"var201", "undefined"},
      /* 202 */ {"APCPN", "Total precipitation (nearest grid point) [kg/m^2]"},
      /* 203 */ {"RSMIN", "Minimal stomatal resistance [s/m]"},
      /* 204 */ {"DSWRF", "Downward shortwave radiation flux [W/m^2]"},
      /* 205 */ {"DLWRF", "Downward longwave radiation flux [W/m^2]"},
      /* 206 */ {"ACPCPN", "Convective precipitation (nearest grid point) [kg/m^2]"},
      /* 207 */ {"MSTAV", "Moisture availability [%]"},
      /* 208 */ {"SFEXC", "Exchange coefficient [(kg/m^3)(m/s)]"},
      /* 209 */ {"var209", "undefined"},
      /* 210 */ {"TRANS", "Transpiration [W/m^2]"},
      /* 211 */ {"USWRF", "Upward short wave radiation flux [W/m^2]"},
      /* 212 */ {"ULWRF", "Upward long wave radiation flux [W/m^2]"},
      /* 213 */ {"CDLYR", "Non-convective cloud [%]"},
      /* 214 */ {"CPRAT", "Convective precip. rate [kg/m^2/s]"},
      /* 215 */ {"var215", "undefined"},
      /* 216 */ {"TTRAD", "Temp. tendency by all radiation [K/s]"},
      /* 217 */ {"var217", "undefined"},
      /* 218 */ {"HGTN", "Geopotential Height (nearest grid point) [gpm]"},
      /* 219 */ {"WILT", "Wilting point [fraction]"},
      /* 220 */ {"FLDCP", "Field Capacity [fraction]"},
      /* 221 */ {"HPBL", "Planetary boundary layer height [m]"},
      /* 222 */ {"SLTYP", "Surface slope type [Index]"},
      /* 223 */ {"CNWAT", "Plant canopy surface water [kg/m^2]"},
      /* 224 */ {"SOTYP", "Soil type [Index]"},
      /* 225 */ {"VGTYP", "Vegetation type [Index]"},
      /* 226 */ {"BMIXL", "Blackadars mixing length scale [m]"},
      /* 227 */ {"AMIXL", "Asymptotic mixing length scale [m]"},
      /* 228 */ {"PEVAP", "Potential evaporation [kg/m^2]"},
      /* 229 */ {"SNOHF", "Snow phase-change heat flux [W/m^2]"},
      /* 230 */ {"SMREF", "Transpiration stress-onset (soil moisture) [fraction]"},
      /* 231 */ {"SMDRY", "Direct evaporation cease (soil moisture) [fraction]"},
      /* 232 */ {"WVINC", "water vapor added by precip assimilation [kg/m^2]"},
      /* 233 */ {"WCINC", "water condensate added by precip assimilaition [kg/m^2]"},
      /* 234 */ {"BGRUN", "Subsurface runoff (baseflow) [kg/m^2]"},
      /* 235 */ {"SSRUN", "Surface runoff (non-infiltrating) [kg/m^2]"},
      /* 236 */ {"var236", "undefined"},
      /* 237 */ {"WVCONV", "Water vapor flux convergence (vertical int) [kg/m^2]"},
      /* 238 */ {"SNOWC", "Snow cover [%]"},
      /* 239 */ {"SNOT", "Snow temperature [K]"},
      /* 240 */ {"POROS", "Soil porosity [fraction]"},
      /* 241 */ {"WCCONV", "Water condensate flux convergence (vertical int) [kg/m^2]"},
      /* 242 */ {"WVUFLX", "Water vapor zonal transport (vertical int)[kg/m]"},
      /* 243 */ {"WVVFLX", "Water vapor meridional transport (vertical int) [kg/m]"},
      /* 244 */ {"WCUFLX", "Water condensate zonal transport (vertical int) [kg/m]"},
      /* 245 */ {"WCVFLX", "Water condensate meridional transport (vertical int) [kg/m]"},
      /* 246 */ {"RCS", "Solar parameter in canopy conductance [fraction]"},
      /* 247 */ {"RCT", "Temperature parameter in canopy conductance [fraction]"},
      /* 248 */ {"RCQ", "Humidity parameter in canopy conductance [fraction]"},
      /* 249 */ {"RCSOL", "Soil moisture parameter in canopy conductance [fraction]"},
      /* 250 */ {"SWHR", "Solar radiative heating [K/s]"},
      /* 251 */ {"LWHR", "Longwave radiative heating [K/s]"},
      /* 252 */ {"CD", "Surface drag coefficient [non-dim]"},
      /* 253 */ {"FRICV", "Surface friction velocity [m/s]"},
      /* 254 */ {"RI", "Richardson number [non-dim]"},
      /* 255 */ {"var255", "undefined"},
};

struct ParmTable parm_table_nceptab_130[256] = {
      /* 0 */ {"var0", "undefined"},
      /* 1 */ {"PRES", "Pressure [Pa]"},
      /* 2 */ {"PRMSL", "Pressure reduced to MSL [Pa]"},
      /* 3 */ {"PTEND", "Pressure tendency [Pa/s]"},
      /* 4 */ {"PVORT", "Pot. vorticity [km^2/kg/s]"},
      /* 5 */ {"ICAHT", "ICAO Standard Atmosphere Reference Height [M]"},
      /* 6 */ {"GP", "Geopotential [m^2/s^2]"},
      /* 7 */ {"HGT", "Geopotential height [gpm]"},
      /* 8 */ {"DIST", "Geometric height [m]"},
      /* 9 */ {"HSTDV", "Std dev of height [m]"},
      /* 10 */ {"TOZNE", "Total ozone [Dobson]"},
      /* 11 */ {"TMP", "Temp. [K]"},
      /* 12 */ {"VTMP", "Virtual temp. [K]"},
      /* 13 */ {"POT", "Potential temp. [K]"},
      /* 14 */ {"EPOT", "Pseudo-adiabatic pot. temp. [K]"},
      /* 15 */ {"TMAX", "Max. temp. [K]"},
      /* 16 */ {"TMIN", "Min. temp. [K]"},
      /* 17 */ {"DPT", "Dew point temp. [K]"},
      /* 18 */ {"DEPR", "Dew point depression [K]"},
      /* 19 */ {"LAPR", "Lapse rate [K/m]"},
      /* 20 */ {"VIS", "Visibility [m]"},
      /* 21 */ {"RDSP1", "Radar spectra (1) [non-dim]"},
      /* 22 */ {"RDSP2", "Radar spectra (2) [non-dim]"},
      /* 23 */ {"RDSP3", "Radar spectra (3) [non-dim]"},
      /* 24 */ {"PLI", "Parcel lifted index (to 500 hPa) [K]"},
      /* 25 */ {"TMPA", "Temp. anomaly [K]"},
      /* 26 */ {"PRESA", "Pressure anomaly [Pa]"},
      /* 27 */ {"GPA", "Geopotential height anomaly [gpm]"},
      /* 28 */ {"WVSP1", "Wave spectra (1) [non-dim]"},
      /* 29 */ {"WVSP2", "Wave spectra (2) [non-dim]"},
      /* 30 */ {"WVSP3", "Wave spectra (3) [non-dim]"},
      /* 31 */ {"WDIR", "Wind direction [deg]"},
      /* 32 */ {"WIND", "Wind speed [m/s]"},
      /* 33 */ {"UGRD", "u wind [m/s]"},
      /* 34 */ {"VGRD", "v wind [m/s]"},
      /* 35 */ {"STRM", "Stream function [m^2/s]"},
      /* 36 */ {"VPOT", "Velocity potential [m^2/s]"},
      /* 37 */ {"MNTSF", "Montgomery stream function [m^2/s^2]"},
      /* 38 */ {"SGCVV", "Sigma coord. vertical velocity [/s]"},
      /* 39 */ {"VVEL", "Pressure vertical velocity [Pa/s]"},
      /* 40 */ {"DZDT", "Geometric vertical velocity [m/s]"},
      /* 41 */ {"ABSV", "Absolute vorticity [/s]"},
      /* 42 */ {"ABSD", "Absolute divergence [/s]"},
      /* 43 */ {"RELV", "Relative vorticity [/s]"},
      /* 44 */ {"RELD", "Relative divergence [/s]"},
      /* 45 */ {"VUCSH", "Vertical u shear [/s]"},
      /* 46 */ {"VVCSH", "Vertical v shear [/s]"},
      /* 47 */ {"DIRC", "Direction of current [deg]"},
      /* 48 */ {"SPC", "Speed of current [m/s]"},
      /* 49 */ {"UOGRD", "u of current [m/s]"},
      /* 50 */ {"VOGRD", "v of current [m/s]"},
      /* 51 */ {"SPFH", "Specific humidity [kg/kg]"},
      /* 52 */ {"RH", "Relative humidity [%]"},
      /* 53 */ {"MIXR", "Humidity mixing ratio [kg/kg]"},
      /* 54 */ {"PWAT", "Precipitable water [kg/m^2]"},
      /* 55 */ {"VAPP", "Vapor pressure [Pa]"},
      /* 56 */ {"SATD", "Saturation deficit [Pa]"},
      /* 57 */ {"EVP", "Evaporation [kg/m^2]"},
      /* 58 */ {"CICE", "Cloud Ice [kg/m^2]"},
      /* 59 */ {"PRATE", "Precipitation rate [kg/m^2/s]"},
      /* 60 */ {"TSTM", "Thunderstorm probability [%]"},
      /* 61 */ {"APCP", "Total precipitation [kg/m^2]"},
      /* 62 */ {"NCPCP", "Large scale precipitation [kg/m^2]"},
      /* 63 */ {"ACPCP", "Convective precipitation [kg/m^2]"},
      /* 64 */ {"SRWEQ", "Snowfall rate water equiv. [kg/m^2/s]"},
      /* 65 */ {"WEASD", "Accum. snow [kg/m^2]"},
      /* 66 */ {"SNOD", "Snow depth [m]"},
      /* 67 */ {"MIXHT", "Mixed layer depth [m]"},
      /* 68 */ {"TTHDP", "Transient thermocline depth [m]"},
      /* 69 */ {"MTHD", "Main thermocline depth [m]"},
      /* 70 */ {"MTHA", "Main thermocline anomaly [m]"},
      /* 71 */ {"TCDC", "Total cloud cover [%]"},
      /* 72 */ {"CDCON", "Convective cloud cover [%]"},
      /* 73 */ {"LCDC", "Low level cloud cover [%]"},
      /* 74 */ {"MCDC", "Mid level cloud cover [%]"},
      /* 75 */ {"HCDC", "High level cloud cover [%]"},
      /* 76 */ {"CWAT", "Cloud water [kg/m^2]"},
      /* 77 */ {"BLI", "Best lifted index (to 500 hPa) [K]"},
      /* 78 */ {"SNOC", "Convective snow [kg/m^2]"},
      /* 79 */ {"SNOL", "Large scale snow [kg/m^2]"},
      /* 80 */ {"WTMP", "Water temp. [K]"},
      /* 81 */ {"LAND", "Land cover (land=1;sea=0) [fraction]"},
      /* 82 */ {"DSLM", "Deviation of sea level from mean [m]"},
      /* 83 */ {"SFCR", "Surface roughness [m]"},
      /* 84 */ {"ALBDO", "Albedo [%]"},
      /* 85 */ {"TSOIL", "Soil temp. [K]"},
      /* 86 */ {"SOILM", "Soil moisture content [kg/m^2]"},
      /* 87 */ {"VEG", "Vegetation [%]"},
      /* 88 */ {"SALTY", "Salinity [kg/kg]"},
      /* 89 */ {"DEN", "Density [kg/m^3]"},
      /* 90 */ {"WATR", "Water runoff [kg/m^2]"},
      /* 91 */ {"ICEC", "Ice concentration (ice=1;no ice=0) [fraction]"},
      /* 92 */ {"ICETK", "Ice thickness [m]"},
      /* 93 */ {"DICED", "Direction of ice drift [deg]"},
      /* 94 */ {"SICED", "Speed of ice drift [m/s]"},
      /* 95 */ {"UICE", "u of ice drift [m/s]"},
      /* 96 */ {"VICE", "v of ice drift [m/s]"},
      /* 97 */ {"ICEG", "Ice growth rate [m/s]"},
      /* 98 */ {"ICED", "Ice divergence [/s]"},
      /* 99 */ {"SNOM", "Snow melt [kg/m^2]"},
      /* 100 */ {"HTSGW", "Sig height of wind waves and swell [m]"},
      /* 101 */ {"WVDIR", "Direction of wind waves [deg]"},
      /* 102 */ {"WVHGT", "Sig height of wind waves [m]"},
      /* 103 */ {"WVPER", "Mean period of wind waves [s]"},
      /* 104 */ {"SWDIR", "Direction of swell waves [deg]"},
      /* 105 */ {"SWELL", "Sig height of swell waves [m]"},
      /* 106 */ {"SWPER", "Mean period of swell waves [s]"},
      /* 107 */ {"DIRPW", "Primary wave direction [deg]"},
      /* 108 */ {"PERPW", "Primary wave mean period [s]"},
      /* 109 */ {"DIRSW", "Secondary wave direction [deg]"},
      /* 110 */ {"PERSW", "Secondary wave mean period [s]"},
      /* 111 */ {"NSWRS", "Net short wave (surface) [W/m^2]"},
      /* 112 */ {"NLWRS", "Net long wave (surface) [W/m^2]"},
      /* 113 */ {"NSWRT", "Net short wave (top) [W/m^2]"},
      /* 114 */ {"NLWRT", "Net long wave (top) [W/m^2]"},
      /* 115 */ {"LWAVR", "Long wave [W/m^2]"},
      /* 116 */ {"SWAVR", "Short wave [W/m^2]"},
      /* 117 */ {"GRAD", "Global radiation [W/m^2]"},
      /* 118 */ {"BRTMP", "Brightness temperature [K]"},
      /* 119 */ {"LWRAD", "Radiance with respect to wave no. [W/m/sr]"},
      /* 120 */ {"SWRAD", "Radiance with respect ot wave len. [W/m^3/sr]"},
      /* 121 */ {"LHTFL", "Latent heat flux [W/m^2]"},
      /* 122 */ {"SHTFL", "Sensible heat flux [W/m^2]"},
      /* 123 */ {"BLYDP", "Boundary layer dissipation [W/m^2]"},
      /* 124 */ {"UFLX", "Zonal momentum flux [N/m^2]"},
      /* 125 */ {"VFLX", "Meridional momentum flux [N/m^2]"},
      /* 126 */ {"WMIXE", "Wind mixing energy [J]"},
      /* 127 */ {"IMGD", "Image data []"},
      /* 128 */ {"var128", "undefined"},
      /* 129 */ {"var129", "undefined"},
      /* 130 */ {"var130", "undefined"},
      /* 131 */ {"var131", "undefined"},
      /* 132 */ {"var132", "undefined"},
      /* 133 */ {"var133", "undefined"},
      /* 134 */ {"var134", "undefined"},
      /* 135 */ {"var135", "undefined"},
      /* 136 */ {"var136", "undefined"},
      /* 137 */ {"var137", "undefined"},
      /* 138 */ {"var138", "undefined"},
      /* 139 */ {"var139", "undefined"},
      /* 140 */ {"var140", "undefined"},
      /* 141 */ {"var141", "undefined"},
      /* 142 */ {"var142", "undefined"},
      /* 143 */ {"var143", "undefined 143"},
      /* 144 */ {"SOILW", "Volumetric soil moisture (frozen + liquid) [fraction]"},
      /* 145 */ {"PEVPR", "Potential evaporation rate [W/m^2]"},
      /* 146 */ {"VEGT", "Vegetation canopy temperature [K]"},
      /* 147 */ {"BARET", "Bare soil surface skin temperature [K]"},
      /* 148 */ {"AVSFT", "Average surface skin temperature [K]"},
      /* 149 */ {"RADT", "Effective radiative skin temperature [K]"},
      /* 150 */ {"SSTOR", "Surface water storage [Kg/m^2]"},
      /* 151 */ {"LSOIL", "Liquid soil moisture content (non-frozen) [Kg/m^2]"},
      /* 152 */ {"EWATR", "Open water evaporation (standing water) [W/m^2]"},
      /* 153 */ {"var153", "undefined"},
      /* 154 */ {"var154", "undefined"},
      /* 155 */ {"GFLUX", "Ground Heat Flux [W/m^2]"},
      /* 156 */ {"CIN", "Convective inhibition [J/Kg]"},
      /* 157 */ {"CAPE", "Convective available potential energy [J/Kg]"},
      /* 158 */ {"TKE", "Turbulent Kinetic Energy [J/Kg]"},
      /* 159 */ {"MXSALB", "Maximum snow albedo [%]"},
      /* 160 */ {"SOILL", "Liquid volumetric soil moisture (non-frozen) [fraction]"},
      /* 161 */ {"ASNOW", "Frozen precipitation (e.g. snowfall) [Kg/m^2]"},
      /* 162 */ {"ARAIN", "Liquid precipitation (rainfall) [Kg/m^2]"},
      /* 163 */ {"GWREC", "Groundwater recharge [Kg/m^2]"},
      /* 164 */ {"QREC", "Flood plain recharge [Kg/m^2]"},
      /* 165 */ {"SNOWT", "Snow temperature, depth-avg [K]"},
      /* 166 */ {"VBDSF", "Visible beam downward solar flux [W/m^2]"},
      /* 167 */ {"VDDSF", "Visible diffuse downward solar flux [W/m^2]"},
      /* 168 */ {"NBDSF", "Near IR beam downward solar flux [W/m^2]"},
      /* 169 */ {"NDDSF", "Near IR diffuse downward solar flux [W/m^2]"},
      /* 170 */ {"SNFALB", "Snow-free albedo [%]"},
      /* 171 */ {"RLYRS", "Number of soil layers in root zone [non-dim]"},
      /* 172 */ {"MFLX", "Momentum flux [N/m^2]"},
      /* 173 */ {"var173", "undefined"},
      /* 174 */ {"var174", "undefined"},
      /* 175 */ {"var175", "undefined"},
      /* 176 */ {"NLAT", "Latitude (-90 to +90) [deg]"},
      /* 177 */ {"ELON", "East longitude (0-360) [deg]"},
      /* 178 */ {"var178", "undefined"},
      /* 179 */ {"ACOND", "Aerodynamic conductance [m/s]"},
      /* 180 */ {"SNOAG", "Snow age [s]"},
      /* 181 */ {"CCOND", "Canopy conductance [m/s]"},
      /* 182 */ {"LAI", "Leaf area index (0-9) [non-dim]"},
      /* 183 */ {"SFCRH", "Roughness length for heat [m]"},
      /* 184 */ {"SALBD", "Snow albedo (over snow cover area only) [%]"},
      /* 185 */ {"var185", "undefined"},
      /* 186 */ {"var186", "undefined"},
      /* 187 */ {"NDVI", "Normalized Difference Vegetation Index []"},
      /* 188 */ {"DRIP", "Canopy drip [Kg/m^2]"},
      /* 189 */ {"var189", "undefined"},
      /* 190 */ {"var190", "undefined"},
      /* 191 */ {"var191", "undefined"},
      /* 192 */ {"var192", "undefined"},
      /* 193 */ {"var193", "undefined"},
      /* 194 */ {"var194", "undefined"},
      /* 195 */ {"var195", "undefined"},
      /* 196 */ {"var196", "undefined"},
      /* 197 */ {"var197", "undefined"},
      /* 198 */ {"SBSNO", "Sublimation (evaporation from snow) [W/m^2]"},
      /* 199 */ {"EVBS", "Direct evaporation from bare soil [W/m^2]"},
      /* 200 */ {"EVCW", "Canopy water evaporation [W/m^2]"},
      /* 201 */ {"var201", "undefined"},
      /* 202 */ {"var202", "undefined"},
      /* 203 */ {"RSMIN", "Minimal stomatal resistance [s/m]"},
      /* 204 */ {"DSWRF", "Downward shortwave radiation flux [W/m^2]"},
      /* 205 */ {"DLWRF", "Downward longwave radiation flux [W/m^2]"},
      /* 206 */ {"var206", "undefined"},
      /* 207 */ {"MSTAV", "Moisture availability [%]"},
      /* 208 */ {"SFEXC", "Exchange coefficient [(Kg/m^3)(m/s)]"},
      /* 209 */ {"var209", "undefined"},
      /* 210 */ {"TRANS", "Transpiration [W/m^2]"},
      /* 211 */ {"USWRF", "Upward short wave radiation flux [W/m^2]"},
      /* 212 */ {"ULWRF", "Upward long wave radiation flux [W/m^2]"},
      /* 213 */ {"var213", "undefined"},
      /* 214 */ {"var214", "undefined"},
      /* 215 */ {"var215", "undefined"},
      /* 216 */ {"var216", "undefined"},
      /* 217 */ {"var217", "undefined"},
      /* 218 */ {"var218", "undefined"},
      /* 219 */ {"WILT", "Wilting point [fraction]"},
      /* 220 */ {"FLDCP", "Field Capacity [fraction]"},
      /* 221 */ {"HPBL", "Planetary boundary layer height [m]"},
      /* 222 */ {"SLTYP", "Surface slope type [Index]"},
      /* 223 */ {"CNWAT", "Plant canopy surface water [Kg/m^2]"},
      /* 224 */ {"SOTYP", "Soil type [Index]"},
      /* 225 */ {"VGTYP", "Vegetation type [Index]"},
      /* 226 */ {"BMIXL", "Blackadars mixing length scale [m]"},
      /* 227 */ {"AMIXL", "Asymptotic mixing length scale [m]"},
      /* 228 */ {"PEVAP", "Potential evaporation [Kg/m^2]"},
      /* 229 */ {"SNOHF", "Snow phase-change heat flux [W/m^2]"},
      /* 230 */ {"SMREF", "Transpiration stress-onset (soil moisture) [fraction]"},
      /* 231 */ {"SMDRY", "Direct evaporation cease (soil moisture) [fraction]"},
      /* 232 */ {"var232", "undefined"},
      /* 233 */ {"var233", "undefined"},
      /* 234 */ {"BGRUN", "Subsurface runoff (baseflow) [Kg/m^2]"},
      /* 235 */ {"SSRUN", "Surface runoff (non-infiltrating) [Kg/m^2]"},
      /* 236 */ {"var236", "undefined"},
      /* 237 */ {"var237", "undefined"},
      /* 238 */ {"SNOWC", "Snow cover [%]"},
      /* 239 */ {"SNOT", "Snow temperature [K]"},
      /* 240 */ {"POROS", "Soil porosity [fraction]"},
      /* 241 */ {"var241", "undefined"},
      /* 242 */ {"var242", "undefined"},
      /* 243 */ {"var243", "undefined"},
      /* 244 */ {"var244", "undefined"},
      /* 245 */ {"var245", "undefined"},
      /* 246 */ {"RCS", "Solar parameter in canopy conductance [fraction]"},
      /* 247 */ {"RCT", "Temperature parameter in canopy conductance [fraction]"},
      /* 248 */ {"RCQ", "Humidity parameter in canopy conductance [fraction]"},
      /* 249 */ {"RCSOL", "Soil moisture parameter in canopy conductance [fraction]"},
      /* 250 */ {"var250", "undefined"},
      /* 251 */ {"var251", "undefined"},
      /* 252 */ {"CD", "Surface drag coefficient [non-dim]"},
      /* 253 */ {"FRICV", "Surface friction velocity [m/s]"},
      /* 254 */ {"RI", "Richardson number [non-dim]"},
      /* 255 */ {"var255", "undefined"},
};

/*
  Helmut Frank, updated 24.07.2003: UVB, PAR, CAPE
*/

struct ParmTable parm_table_ecmwf_128[256] = {
      /* 0 */ {"var0", "undefined"},
      /* 1 */ {"STRF", "Stream function [m**2 s**-1]"},
      /* 2 */ {"VPOT", "Velocity potential [m**2 s**-1]"},
      /* 3 */ {"PT", "Potential temperature [K]"},
      /* 4 */ {"EQPT", "Equivalent potential temperature [K]"},
      /* 5 */ {"SEPT", "Saturated equivalent potential temperature [K]"},
      /* 6 */ {"var6", "Reserved for Metview"},
      /* 7 */ {"var7", "Reserved for Metview"},
      /* 8 */ {"var8", "Reserved for Metview"},
      /* 9 */ {"var9", "Reserved for Metview"},
      /* 10 */ {"var10", "Reserved for Metview"},
      /* 11 */ {"UDVW", "U component of divergent wind [m s**-1]"},
      /* 12 */ {"VDVW", "V component of divergent wind [m s**-1]"},
      /* 13 */ {"URTW", "U component of rotational wind [m s**-1]"},
      /* 14 */ {"VRTW", "V component of rotational wind [m s**-1]"},
      /* 15 */ {"var15", "Reserved for Metview"},
      /* 16 */ {"var16", "Reserved for Metview"},
      /* 17 */ {"var17", "Reserved for Metview"},
      /* 18 */ {"var18", "Reserved for Metview"},
      /* 19 */ {"var19", "Reserved for Metview"},
      /* 20 */ {"var20", "Reserved for Metview"},
      /* 21 */ {"UCTP", "Unbalanced component of temperature [K]"},
      /* 22 */ {"UCLN", "Unbalanced component of logarithm of surface pressure"},
      /* 23 */ {"UCDV", "Unbalanced component of divergence [s**-1]"},
      /* 24 */ {"var24", "Reserved for future unbalanced components"},
      /* 25 */ {"var25", "Reserved for future unbalanced components"},
      /* 26 */ {"CL", "Lake cover [(0-1)]"},
      /* 27 */ {"CVL", "Low vegetation cover [(0-1)]"},
      /* 28 */ {"CVH", "High vegetation cover [(0-1)]"},
      /* 29 */ {"TVL", "Type of low vegetation"},
      /* 30 */ {"TVH", "Type of high vegetation"},
      /* 31 */ {"CI", "Sea-ice cover [(0-1)]"},
      /* 32 */ {"ASN", "Snow albedo [(0-1)]"},
      /* 33 */ {"RSN", "Snow density [kg m**-3]"},
      /* 34 */ {"SSTK", "Sea surface temperature [K]"},
      /* 35 */ {"ISTL1", "Ice surface temperature layer 1 [K]"},
      /* 36 */ {"ISTL2", "Ice surface temperature layer 2 [K]"},
      /* 37 */ {"ISTL3", "Ice surface temperature layer 3 [K]"},
      /* 38 */ {"ISTL4", "Ice surface temperature layer 4 [K]"},
      /* 39 */ {"SWVL1", "Volumetric soil water layer 1 [m**3 m**-3]"},
      /* 40 */ {"SWVL2", "Volumetric soil water layer 2 [m**3 m**-3]"},
      /* 41 */ {"SWVL3", "Volumetric soil water layer 3 [m**3 m**-3]"},
      /* 42 */ {"SWVL4", "Volumetric soil water layer 4 [m**3 m**-3]"},
      /* 43 */ {"SLT", "Soil type"},
      /* 44 */ {"ES", "Snow evaporation [m of water]"},
      /* 45 */ {"SMLT", "Snowmelt [m of water]"},
      /* 46 */ {"SDUR", "Solar duration [s]"},
      /* 47 */ {"DSRP", "Direct solar radiation [w m**-2]"},
      /* 48 */ {"MAGSS", "Magnitude of surface stress [N m**-2 s]"},
      /* 49 */ {"10FG", "Wind gust at 10 metres [m s**-1]"},
      /* 50 */ {"LSPF", "Large-scale precipitation fraction [s]"},
      /* 51 */ {"MX2T24", "Maximum 2 metre temperature [K]"},
      /* 52 */ {"MN2T24", "Minimum 2 metre temperature [K]"},
      /* 53 */ {"MONT", "Montgomery potential [m**2 s**-2]"},
      /* 54 */ {"PRES", "Pressure [Pa]"},
      /* 55 */ {"var55", "undefined"},
      /* 56 */ {"var56", "undefined"},
      /* 57 */ {"UVB", "Downward UV radiation at the surface (Ultra-violet band B) [W m**-2]"},
      /* 58 */ {"PAR", "Photosynthetically active radiation at the surface [W m**-2]"},
      /* 59 */ {"CAPE", "Convective available potential energy [J kg**-1]"},
      /* 60 */ {"PV", "Potential vorticity [K m**2 kg**-1 s**-1]"},
      /* 61 */ {"var61", "undefined"},
      /* 62 */ {"var62", "undefined"},
      /* 63 */ {"var63", "undefined"},
      /* 64 */ {"var64", "undefined"},
      /* 65 */ {"var65", "undefined"},
      /* 66 */ {"var66", "undefined"},
      /* 67 */ {"var67", "undefined"},
      /* 68 */ {"var68", "undefined"},
      /* 69 */ {"var69", "undefined"},
      /* 70 */ {"var70", "undefined"},
      /* 71 */ {"var71", "undefined"},
      /* 72 */ {"var72", "undefined"},
      /* 73 */ {"var73", "undefined"},
      /* 74 */ {"var74", "undefined"},
      /* 75 */ {"var75", "undefined"},
      /* 76 */ {"var76", "undefined"},
      /* 77 */ {"var77", "undefined"},
      /* 78 */ {"var78", "undefined"},
      /* 79 */ {"var79", "undefined"},
      /* 80 */ {"var80", "undefined"},
      /* 81 */ {"var81", "undefined"},
      /* 82 */ {"var82", "undefined"},
      /* 83 */ {"var83", "undefined"},
      /* 84 */ {"var84", "undefined"},
      /* 85 */ {"var85", "undefined"},
      /* 86 */ {"var86", "undefined"},
      /* 87 */ {"var87", "undefined"},
      /* 88 */ {"var88", "undefined"},
      /* 89 */ {"var89", "undefined"},
      /* 90 */ {"var90", "undefined"},
      /* 91 */ {"var91", "undefined"},
      /* 92 */ {"var92", "undefined"},
      /* 93 */ {"var93", "undefined"},
      /* 94 */ {"var94", "undefined"},
      /* 95 */ {"var95", "undefined"},
      /* 96 */ {"var96", "undefined"},
      /* 97 */ {"var97", "undefined"},
      /* 98 */ {"var98", "undefined"},
      /* 99 */ {"var99", "undefined"},
      /* 100 */ {"100", "Experimental product [Undefined]"},
      /* 101 */ {"101", "Experimental product [Undefined]"},
      /* 102 */ {"102", "Experimental product [Undefined]"},
      /* 103 */ {"103", "Experimental product [Undefined]"},
      /* 104 */ {"104", "Experimental product [Undefined]"},
      /* 105 */ {"105", "Experimental product [Undefined]"},
      /* 106 */ {"106", "Experimental product [Undefined]"},
      /* 107 */ {"107", "Experimental product [Undefined]"},
      /* 108 */ {"108", "Experimental product [Undefined]"},
      /* 109 */ {"109", "Experimental product [Undefined]"},
      /* 110 */ {"110", "Experimental product [Undefined]"},
      /* 111 */ {"111", "Experimental product [Undefined]"},
      /* 112 */ {"112", "Experimental product [Undefined]"},
      /* 113 */ {"113", "Experimental product [Undefined]"},
      /* 114 */ {"114", "Experimental product [Undefined]"},
      /* 115 */ {"115", "Experimental product [Undefined]"},
      /* 116 */ {"116", "Experimental product [Undefined]"},
      /* 117 */ {"117", "Experimental product [Undefined]"},
      /* 118 */ {"118", "Experimental product [Undefined]"},
      /* 119 */ {"119", "Experimental product [Undefined]"},
      /* 120 */ {"120", "Experimental product [Undefined]"},
      /* 121 */ {"var121", "undefined"},
      /* 122 */ {"var122", "undefined"},
      /* 123 */ {"var123", "undefined"},
      /* 124 */ {"var124", "undefined"},
      /* 125 */ {"var125", "undefined"},
      /* 126 */ {"var126", "undefined"},
      /* 127 */ {"AT", "Atmospheric tide"},
      /* 128 */ {"BV", "Budget values"},
      /* 129 */ {"Z", "Geopotential [m**2 s**-2]"},
      /* 130 */ {"T", "Temperature [K]"},
      /* 131 */ {"U", "U velocity [m s**-1]"},
      /* 132 */ {"V", "V velocity [m s**-1]"},
      /* 133 */ {"Q", "Specific humidity [kg kg**-1]"},
      /* 134 */ {"SP", "Surface pressure [Pa]"},
      /* 135 */ {"W", "Vertical velocity [Pa s**-1]"},
      /* 136 */ {"TCW", "Total column water [kg m**-2]"},
      /* 137 */ {"TCWV", "Total column water vapour [kg m**-2]"},
      /* 138 */ {"VO", "Vorticity (relative) [s**-1]"},
      /* 139 */ {"STL1", "Soil temperature level 1 [K]"},
      /* 140 */ {"SWL1", "Soil wetness level 1 [m of water]"},
      /* 141 */ {"SD", "Snow depth [m of water equivalent]"},
      /* 142 */ {"LSP", "Stratiform precipitation [m]"},
      /* 143 */ {"CP", "Convective precipitation [m]"},
      /* 144 */ {"SF", "Snowfall (convective + stratiform) [m of water equivalent]"},
      /* 145 */ {"BLD", "Boundary layer dissipation [W m**-2 s]"},
      /* 146 */ {"SSHF", "Surface sensible heat flux [W m**-2 s]"},
      /* 147 */ {"SLHF", "Surface latent heat flux [W m**-2 s]"},
      /* 148 */ {"CHNK", "Charnock"},
      /* 149 */ {"SNR", "Surface net radiation [W m**-2 s]"},
      /* 150 */ {"TNR", "Top net radiation"},
      /* 151 */ {"MSL", "Mean sea-level pressure [Pa]"},
      /* 152 */ {"LNSP", "Logarithm of surface pressure"},
      /* 153 */ {"SWHR", "Short-wave heating rate [K]"},
      /* 154 */ {"LWHR", "Long-wave heating rate [K]"},
      /* 155 */ {"D", "Divergence [s**-1]"},
      /* 156 */ {"GH", "Height [m]"},
      /* 157 */ {"R", "Relative humidity [%]"},
      /* 158 */ {"TSP", "Tendency of surface pressure [Pa s**-1]"},
      /* 159 */ {"BLH", "Boundary layer height [m]"},
      /* 160 */ {"SDOR", "Standard deviation of orography"},
      /* 161 */ {"ISOR", "Anisotropy of sub-gridscale orography"},
      /* 162 */ {"ANOR", "Angle of sub-gridscale orography [rad]"},
      /* 163 */ {"SLOR", "Slope of sub-gridscale orography"},
      /* 164 */ {"TCC", "Total cloud cover [(0 - 1)]"},
      /* 165 */ {"10U", "10 metre U wind component [m s**-1]"},
      /* 166 */ {"10V", "10 metre V wind component [m s**-1]"},
      /* 167 */ {"2T", "2 metre temperature [K]"},
      /* 168 */ {"2D", "2 metre dewpoint temperature [K]"},
      /* 169 */ {"SSRD", "Surface solar radiation downwards [W m**-2 s]"},
      /* 170 */ {"STL2", "Soil temperature level 2 [K]"},
      /* 171 */ {"SWL2", "Soil wetness level 2 [m of water]"},
      /* 172 */ {"LSM", "Land/sea mask [(0, 1)]"},
      /* 173 */ {"SR", "Surface roughness [m]"},
      /* 174 */ {"AL", "Albedo [(0 - 1)]"},
      /* 175 */ {"STRD", "Surface thermal radiation downwards [W m**-2 s]"},
      /* 176 */ {"SSR", "Surface solar radiation [W m**-2 s]"},
      /* 177 */ {"STR", "Surface thermal radiation [W m**-2 s]"},
      /* 178 */ {"TSR", "Top solar radiation [W m**-2 s]"},
      /* 179 */ {"TTR", "Top thermal radiation [W m**-2 s]"},
      /* 180 */ {"EWSS", "East/West surface stress [N m**-2 s]"},
      /* 181 */ {"NSSS", "North/South surface stress [N m**-2 s]"},
      /* 182 */ {"E", "Evaporation [m of water]"},
      /* 183 */ {"STL3", "Soil temperature level 3 [K]"},
      /* 184 */ {"SWL3", "Soil wetness level 3 [m of water]"},
      /* 185 */ {"CCC", "Convective cloud cover [(0 - 1)]"},
      /* 186 */ {"LCC", "Low cloud cover [(0 - 1)]"},
      /* 187 */ {"MCC", "Medium cloud cover [(0 - 1)]"},
      /* 188 */ {"HCC", "High cloud cover [(0 - 1)]"},
      /* 189 */ {"SUND", "Sunshine duration [s]"},
      /* 190 */ {"EWOV", "EW component of subgrid orographic variance [m**2]"},
      /* 191 */ {"NSOV", "NS component of subgrid orographic variance [m**2]"},
      /* 192 */ {"NWOV", "NWSE component of subgrid orographic variance [m**2]"},
      /* 193 */ {"NEOV", "NESW component of subgrid orographic variance [m**2]"},
      /* 194 */ {"BTMP", "Brightness temperature [K]"},
      /* 195 */ {"LGWS", "Lat. component of gravity wave stress [N m**-2 s]"},
      /* 196 */ {"MGWS", "Meridional component of gravity wave stress [N m**-2 s]"},
      /* 197 */ {"GWD", "Gravity wave dissipation [W m**-2 s]"},
      /* 198 */ {"SRC", "Skin reservoir content [m of water]"},
      /* 199 */ {"VEG", "Vegetation fraction [(0 - 1)]"},
      /* 200 */ {"VSO", "Variance of sub-gridscale orography [m**2]"},
      /* 201 */ {"MX2T", "Maximum 2 metre temperature since previous post-processing [K]"},
      /* 202 */ {"MN2T", "Minimum 2 metre temperature since previous post-processing [K]"},
      /* 203 */ {"O3", "Ozone mass mixing ratio [kg kg**-1]"},
      /* 204 */ {"PAW", "Precipiation analysis weights"},
      /* 205 */ {"RO", "Runoff [m]"},
      /* 206 */ {"TCO3", "Total column ozone [Dobson]"},
      /* 207 */ {"10SI", "10 meter windspeed [m s**-1]"},
      /* 208 */ {"TSRC", "Top net solar radiation, clear sky [W m**-2]"},
      /* 209 */ {"TTRC", "Top net thermal radiation, clear sky [W m**-2]"},
      /* 210 */ {"SSRC", "Surface net solar radiation, clear sky [W m**-2]"},
      /* 211 */ {"STRC", "Surface net thermal radiation, clear sky [W m**-2]"},
      /* 212 */ {"SI", "Solar insolation [W m**-2]"},
      /* 213 */ {"var213", "undefined"},
      /* 214 */ {"DHR", "Diabatic heating by radiation [K]"},
      /* 215 */ {"DHVD", "Diabatic heating by vertical diffusion [K]"},
      /* 216 */ {"DHCC", "Diabatic heating by cumulus convection [K]"},
      /* 217 */ {"DHLC", "Diabatic heating large-scale condensation [K]"},
      /* 218 */ {"VDZW", "Vertical diffusion of zonal wind [m s**-1]"},
      /* 219 */ {"VDMW", "Vertical diffusion of meridional wind [m s**-1]"},
      /* 220 */ {"EWGD", "EW gravity wave drag tendency [m s**-1]"},
      /* 221 */ {"NSGD", "NS gravity wave drag tendency [m s**-1]"},
      /* 222 */ {"CTZW", "Convective tendency of zonal wind [m s**-1]"},
      /* 223 */ {"CTMW", "Convective tendency of meridional wind [m s**-1]"},
      /* 224 */ {"VDH", "Vertical diffusion of humidity [kg kg**-1]"},
      /* 225 */ {"HTCC", "Humidity tendency by cumulus convection [kg kg**-1]"},
      /* 226 */ {"HTLC", "Humidity tendency large-scale condensation [kg kg**-1]"},
      /* 227 */ {"CRNH", "Change from removing negative humidity [kg kg**-1]"},
      /* 228 */ {"TP", "Total precipitation [m]"},
      /* 229 */ {"IEWS", "Instantaneous X surface stress [N m**-2]"},
      /* 230 */ {"INSS", "Instantaneous Y surface stress [N m**-2]"},
      /* 231 */ {"ISHF", "Instantaneous surface heat flux [W m**-2]"},
      /* 232 */ {"IE", "Instantaneous moisture flux [kg m**-2 s]"},
      /* 233 */ {"ASQ", "Apparent surface humidity [kg kg**-1]"},
      /* 234 */ {"LSRH", "Logarithm of surface roughness length for heat"},
      /* 235 */ {"SKT", "Skin temperature [K]"},
      /* 236 */ {"STL4", "Soil temperature level 4 [K]"},
      /* 237 */ {"SWL4", "Soil wetness level 4 [m]"},
      /* 238 */ {"TSN", "Temperature of snow layer [K]"},
      /* 239 */ {"CSF", "Convective snowfall [m of water equivalent]"},
      /* 240 */ {"LSF", "Large-scale snowfall [m of water equivalent]"},
      /* 241 */ {"ACF", "Accumulated cloud fraction tendency [(-1 to 1)]"},
      /* 242 */ {"ALW", "Accumulated liquid water tendency [(-1 to 1)]"},
      /* 243 */ {"FAL", "Forecast albedo [(0 - 1)]"},
      /* 244 */ {"FSR", "Forecast surface roughness [m]"},
      /* 245 */ {"FLSR", "Forecast log of surface roughness for heat"},
      /* 246 */ {"CLWC", "Cloud liquid water content [kg kg**-1]"},
      /* 247 */ {"CIWC", "Cloud ice water content [kg kg**-1]"},
      /* 248 */ {"CC", "Cloud cover [(0 - 1)]"},
      /* 249 */ {"AIW", "Accumulated ice water tendency [(-1 to 1)]"},
      /* 250 */ {"ICE", "Ice age [1,0]"},
      /* 251 */ {"ATTE", "Adiabatic tendency of temperature [K]"},
      /* 252 */ {"ATHE", "Adiabatic tendency of humidity [kg kg**-1]"},
      /* 253 */ {"ATZE", "Adiabatic tendency of zonal wind [m s**-1]"},
      /* 254 */ {"ATMW", "Adiabatic tendency of meridional wind [m s**-1]"},
      /* 255 */ {"var255", "Indicates a missing value"},
};


struct ParmTable parm_table_ecmwf_129[256] = {
    /* 0 */ {"var0", "undefined"},
    /* 1 */ {"var1", "undefined"},
    /* 2 */ {"var2", "undefined"},
    /* 3 */ {"var3", "undefined"},
    /* 4 */ {"var4", "undefined"},
    /* 5 */ {"var5", "undefined"},
    /* 6 */ {"var6", "undefined"},
    /* 7 */ {"var7", "undefined"},
    /* 8 */ {"var8", "undefined"},
    /* 9 */ {"var9", "undefined"},
    /* 10 */ {"var10", "undefined"},
    /* 11 */ {"var11", "undefined"},
    /* 12 */ {"var12", "undefined"},
    /* 13 */ {"var13", "undefined"},
    /* 14 */ {"var14", "undefined"},
    /* 15 */ {"var15", "undefined"},
    /* 16 */ {"var16", "undefined"},
    /* 17 */ {"var17", "undefined"},
    /* 18 */ {"var18", "undefined"},
    /* 19 */ {"var19", "undefined"},
    /* 20 */ {"var20", "undefined"},
    /* 21 */ {"var21", "undefined"},
    /* 22 */ {"var22", "undefined"},
    /* 23 */ {"var23", "undefined"},
    /* 24 */ {"var24", "undefined"},
    /* 25 */ {"var25", "undefined"},
    /* 26 */ {"var26", "undefined"},
    /* 27 */ {"var27", "undefined"},
    /* 28 */ {"var28", "undefined"},
    /* 29 */ {"var29", "undefined"},
    /* 30 */ {"var30", "undefined"},
    /* 31 */ {"var31", "undefined"},
    /* 32 */ {"var32", "undefined"},
    /* 33 */ {"var33", "undefined"},
    /* 34 */ {"var34", "undefined"},
    /* 35 */ {"var35", "undefined"},
    /* 36 */ {"var36", "undefined"},
    /* 37 */ {"var37", "undefined"},
    /* 38 */ {"var38", "undefined"},
    /* 39 */ {"var39", "undefined"},
    /* 40 */ {"var40", "undefined"},
    /* 41 */ {"var41", "undefined"},
    /* 42 */ {"var42", "undefined"},
    /* 43 */ {"var43", "undefined"},
    /* 44 */ {"var44", "undefined"},
    /* 45 */ {"var45", "undefined"},
    /* 46 */ {"var46", "undefined"},
    /* 47 */ {"var47", "undefined"},
    /* 48 */ {"var48", "undefined"},
    /* 49 */ {"var49", "undefined"},
    /* 50 */ {"var50", "undefined"},
    /* 51 */ {"var51", "undefined"},
    /* 52 */ {"var52", "undefined"},
    /* 53 */ {"var53", "undefined"},
    /* 54 */ {"var54", "undefined"},
    /* 55 */ {"var55", "undefined"},
    /* 56 */ {"var56", "undefined"},
    /* 57 */ {"var57", "undefined"},
    /* 58 */ {"var58", "undefined"},
    /* 59 */ {"var59", "undefined"},
    /* 60 */ {"var60", "undefined"},
    /* 61 */ {"var61", "undefined"},
    /* 62 */ {"var62", "undefined"},
    /* 63 */ {"var63", "undefined"},
    /* 64 */ {"var64", "undefined"},
    /* 65 */ {"var65", "undefined"},
    /* 66 */ {"var66", "undefined"},
    /* 67 */ {"var67", "undefined"},
    /* 68 */ {"var68", "undefined"},
    /* 69 */ {"var69", "undefined"},
    /* 70 */ {"var70", "undefined"},
    /* 71 */ {"var71", "undefined"},
    /* 72 */ {"var72", "undefined"},
    /* 73 */ {"var73", "undefined"},
    /* 74 */ {"var74", "undefined"},
    /* 75 */ {"var75", "undefined"},
    /* 76 */ {"var76", "undefined"},
    /* 77 */ {"var77", "undefined"},
    /* 78 */ {"var78", "undefined"},
    /* 79 */ {"var79", "undefined"},
    /* 80 */ {"var80", "undefined"},
    /* 81 */ {"var81", "undefined"},
    /* 82 */ {"var82", "undefined"},
    /* 83 */ {"var83", "undefined"},
    /* 84 */ {"var84", "undefined"},
    /* 85 */ {"var85", "undefined"},
    /* 86 */ {"var86", "undefined"},
    /* 87 */ {"var87", "undefined"},
    /* 88 */ {"var88", "undefined"},
    /* 89 */ {"var89", "undefined"},
    /* 90 */ {"var90", "undefined"},
    /* 91 */ {"var91", "undefined"},
    /* 92 */ {"var92", "undefined"},
    /* 93 */ {"var93", "undefined"},
    /* 94 */ {"var94", "undefined"},
    /* 95 */ {"var95", "undefined"},
    /* 96 */ {"var96", "undefined"},
    /* 97 */ {"var97", "undefined"},
    /* 98 */ {"var98", "undefined"},
    /* 99 */ {"var99", "undefined"},
    /* 100 */ {"var100", "undefined"},
    /* 101 */ {"var101", "undefined"},
    /* 102 */ {"var102", "undefined"},
    /* 103 */ {"var103", "undefined"},
    /* 104 */ {"var104", "undefined"},
    /* 105 */ {"var105", "undefined"},
    /* 106 */ {"var106", "undefined"},
    /* 107 */ {"var107", "undefined"},
    /* 108 */ {"var108", "undefined"},
    /* 109 */ {"var109", "undefined"},
    /* 110 */ {"var110", "undefined"},
    /* 111 */ {"var111", "undefined"},
    /* 112 */ {"var112", "undefined"},
    /* 113 */ {"var113", "undefined"},
    /* 114 */ {"var114", "undefined"},
    /* 115 */ {"var115", "undefined"},
    /* 116 */ {"var116", "undefined"},
    /* 117 */ {"var117", "undefined"},
    /* 118 */ {"var118", "undefined"},
    /* 119 */ {"var119", "undefined"},
    /* 120 */ {"var120", "undefined"},
    /* 121 */ {"var121", "undefined"},
    /* 122 */ {"var122", "undefined"},
    /* 123 */ {"var123", "undefined"},
    /* 124 */ {"var124", "undefined"},
    /* 125 */ {"var125", "undefined"},
    /* 126 */ {"var126", "undefined"},
    /* 127 */ {"AT", "Atmospheric tide+ -"},
    /* 128 */ {"BV", "Budget values+ -"},
    /* 129 */ {"Z", "Geopotential (at the surface=orography) m**2 s**-2"},
    /* 130 */ {"T", "Temperature K"},
    /* 131 */ {"U", "U-velocity m s**-1"},
    /* 132 */ {"V", "V-velocity m s**-1"},
    /* 133 */ {"Q", "Specific humidity kg kg**-1"},
    /* 134 */ {"SP", "Surface pressure Pa"},
    /* 135 */ {"W", "Vertical velocity Pa s**-1"},
    /* 136 */ {"var136", "undefined"},
    /* 137 */ {"PWC", "Precipitable water content kg m**-2"},
    /* 138 */ {"VO", "Vorticity (relative) s**-1"},
    /* 139 */ {"ST", "Surf.temp/soil temp lev 1 (from 930804) K"},
    /* 140 */ {"SSW", "Surf soil wet/soil wet lev1(from 930803) m (of water)"},
    /* 141 */ {"SD", "Snow depth m (of water equivalent)"},
    /* 142 */ {"LSP", "Large scale precipitation* m"},
    /* 143 */ {"CP", "Convective precipitation* m"},
    /* 144 */ {"SF", "Snow fall* m(of water equivalent)"},
    /* 145 */ {"BLD", "Boundary layer dissipation* W m**-2 s"},
    /* 146 */ {"SSHF", "Surface sensible heat flux* W m**-2 s"},
    /* 147 */ {"SLHF", "Surface latent heat flux* W m**-2 s"},
    /* 148 */ {"var148", "undefined"},
    /* 149 */ {"var149", "undefined"},
    /* 150 */ {"var150", "undefined"},
    /* 151 */ {"MSL", "Mean sea level pressure Pa"},
    /* 152 */ {"LNSP", "Log surface pressure -"},
    /* 153 */ {"var153", "undefined"},
    /* 154 */ {"var154", "undefined"},
    /* 155 */ {"D", "Divergence s**-1"},
    /* 156 */ {"GH", "Height (geopotential) m"},
    /* 157 */ {"R", "Relative humidity %"},
    /* 158 */ {"TSP", "Tendency of surface pressure Pa s**-1"},
    /* 159 */ {"var159", "undefined"},
    /* 160 */ {"SDOR", "Standard deviation of orography -"},
    /* 161 */ {"ISOR", "Anisotropy of subgrid scale orography -"},
    /* 162 */ {"ANOR", "Angle of subgrid scale orography -"},
    /* 163 */ {"SLOR", "Slope of subgrid scale orography -"},
    /* 164 */ {"TCC", "Total cloud cover (0 - 1)"},
    /* 165 */ {"10U", "10 metre u wind component m s**-1"},
    /* 166 */ {"10V", "10 metre v wind component m s**-1"},
    /* 167 */ {"2T", "2 metre temperature K"},
    /* 168 */ {"2D", "2 metre dewpoint temperature K"},
    /* 169 */ {"var169", "undefined"},
    /* 170 */ {"DST", "Deep soil tmp/soil temp lev2(frm 930804) K"},
    /* 171 */ {"DSW", "Deep soil wet/soil wet lev2(from 930803) m (of water)"},
    /* 172 */ {"LSM", "Land/sea mask (0"},
    /* 173 */ {"SR", "Surface roughness m"},
    /* 174 */ {"AL", "Albedo -"},
    /* 175 */ {"var175", "undefined"},
    /* 176 */ {"SSR", "Surface solar radiation* W m**-2 s"},
    /* 177 */ {"STR", "Surface thermal radiation* W m**-2 s"},
    /* 178 */ {"TSR", "Top solar radiation* W m**-2 s"},
    /* 179 */ {"TTR", "Top thermal radiation* W m**-2 s"},
    /* 180 */ {"EWSS", "East/West surface stress* N m**-2 s"},
    /* 181 */ {"NSSS", "North/South surface stress* N m**-2 s"},
    /* 182 */ {"E", "Evaporation* m (of water)"},
    /* 183 */ {"CDST", "Clim deep soil tmp/soil tmp lev3(930804) K"},
    /* 184 */ {"CDSW", "Clim deep soil wet/soil wet lev3(930803) m (of water)"},
    /* 185 */ {"CCC", "Convective cloud cover (0 - 1)"},
    /* 186 */ {"LCC", "Low cloud cover (0 - 1)"},
    /* 187 */ {"MCC", "Medium cloud cover (0 - 1)"},
    /* 188 */ {"HCC", "High cloud cover (0 - 1)"},
    /* 189 */ {"var189", "undefined"},
    /* 190 */ {"EWOV", "EW component subgrid scale orographic variance m**2"},
    /* 191 */ {"NSOV", "NS component subgrid scale orographic variance m**2"},
    /* 192 */ {"NWOV", "NWSE component subgrid scale orographic variance m**2"},
    /* 193 */ {"NEOV", "NESW component subgrid scale orographic variance m**2"},
    /* 194 */ {"var194", "undefined"},
    /* 195 */ {"LGWS", "Latitudinal component of gravity wave stress* N m**-2 s"},
    /* 196 */ {"MGWS", "Meridional component of gravity wave stress* N m**-2 s"},
    /* 197 */ {"GWD", "Gravity wave dissipation* W m**-2 s"},
    /* 198 */ {"SRC", "Skin reservoir content m (of water)"},
    /* 199 */ {"VEG", "Percentage of vegetation %"},
    /* 200 */ {"VSO", "Variance of sub-grid scale orography m**2"},
    /* 201 */ {"MX2T", "Max temp. at 2m since previous post-processing K"},
    /* 202 */ {"MN2T", "Min temp. at 2m since previous post-processing K"},
    /* 203 */ {"var203", "undefined"},
    /* 204 */ {"PAW", "Precip. analysis weights -"},
    /* 205 */ {"RO", "Runoff* m"},
    /* 206 */ {"var206", "undefined"},
    /* 207 */ {"var207", "undefined"},
    /* 208 */ {"var208", "undefined"},
    /* 209 */ {"var209", "undefined"},
    /* 210 */ {"var210", "undefined"},
    /* 211 */ {"var211", "undefined"},
    /* 212 */ {"var212", "undefined"},
    /* 213 */ {"var213", "undefined"},
    /* 214 */ {"var214", "undefined"},
    /* 215 */ {"var215", "undefined"},
    /* 216 */ {"var216", "undefined"},
    /* 217 */ {"var217", "undefined"},
    /* 218 */ {"var218", "undefined"},
    /* 219 */ {"var219", "undefined"},
    /* 220 */ {"var220", "undefined"},
    /* 221 */ {"var221", "undefined"},
    /* 222 */ {"var222", "undefined"},
    /* 223 */ {"var223", "undefined"},
    /* 224 */ {"var224", "undefined"},
    /* 225 */ {"var225", "undefined"},
    /* 226 */ {"var226", "undefined"},
    /* 227 */ {"var227", "undefined"},
    /* 228 */ {"TP", "Total precipitation? m"},
    /* 229 */ {"IEWS", "Instantaneous X surface stress N m**-2"},
    /* 230 */ {"INSS", "Instantaneous Y surface stress N m**-2"},
    /* 231 */ {"ISHF", "Instantaneous surface Heat Flux W m**-2"},
    /* 232 */ {"IE", "Instantaneous Moisture Flux (evaporation) kg m**-2 s"},
    /* 233 */ {"ASQ", "Apparent Surface Humidity kg kg**-1"},
    /* 234 */ {"LSRH", "Logarithm of surface roughness length for heat -"},
    /* 235 */ {"SKT", "Skin Temperature K"},
    /* 236 */ {"STL4", "Soil temperature level 4 K"},
    /* 237 */ {"SWL4", "Soil wetness level 4 m"},
    /* 238 */ {"TSN", "Temperature of snow layer K"},
    /* 239 */ {"CSF", "Convective snow-fall* m (of water equivalent)"},
    /* 240 */ {"LSF", "Large scale snow-fall* m (of water equivalent)"},
    /* 241 */ {"var241", "undefined"},
    /* 242 */ {"var242", "undefined"},
    /* 243 */ {"FAL", "Forecast albedo -"},
    /* 244 */ {"FSR", "Forecast surface roughness m"},
    /* 245 */ {"FLSR", "Forecast logarithm of surface roughness for heat -"},
    /* 246 */ {"CLWC", "Cloud liquid water content kg kg**-1"},
    /* 247 */ {"CIWC", "Cloud ice water content kg kg**-1"},
    /* 248 */ {"CC", "Cloud cover (0 - 1)"},
    /* 249 */ {"var249", "undefined"},
    /* 250 */ {"", "Ice Age (0 first-year 1 multi-year)"},
    /* 251 */ {"var251", "undefined"},
    /* 252 */ {"var252", "undefined"},
    /* 253 */ {"var253", "undefined"},
    /* 254 */ {"var254", "undefined"},
    /* 255 */ {"var255", "undefined"},
};


struct ParmTable parm_table_ecmwf_130[256] = {
    /* 0 */ {"var0", "undefined"},
    /* 1 */ {"var1", "undefined"},
    /* 2 */ {"var2", "undefined"},
    /* 3 */ {"var3", "undefined"},
    /* 4 */ {"var4", "undefined"},
    /* 5 */ {"var5", "undefined"},
    /* 6 */ {"var6", "undefined"},
    /* 7 */ {"var7", "undefined"},
    /* 8 */ {"var8", "undefined"},
    /* 9 */ {"var9", "undefined"},
    /* 10 */ {"var10", "undefined"},
    /* 11 */ {"var11", "undefined"},
    /* 12 */ {"var12", "undefined"},
    /* 13 */ {"var13", "undefined"},
    /* 14 */ {"var14", "undefined"},
    /* 15 */ {"var15", "undefined"},
    /* 16 */ {"var16", "undefined"},
    /* 17 */ {"var17", "undefined"},
    /* 18 */ {"var18", "undefined"},
    /* 19 */ {"var19", "undefined"},
    /* 20 */ {"var20", "undefined"},
    /* 21 */ {"var21", "undefined"},
    /* 22 */ {"var22", "undefined"},
    /* 23 */ {"var23", "undefined"},
    /* 24 */ {"var24", "undefined"},
    /* 25 */ {"var25", "undefined"},
    /* 26 */ {"var26", "undefined"},
    /* 27 */ {"var27", "undefined"},
    /* 28 */ {"var28", "undefined"},
    /* 29 */ {"var29", "undefined"},
    /* 30 */ {"var30", "undefined"},
    /* 31 */ {"var31", "undefined"},
    /* 32 */ {"var32", "undefined"},
    /* 33 */ {"var33", "undefined"},
    /* 34 */ {"var34", "undefined"},
    /* 35 */ {"var35", "undefined"},
    /* 36 */ {"var36", "undefined"},
    /* 37 */ {"var37", "undefined"},
    /* 38 */ {"var38", "undefined"},
    /* 39 */ {"var39", "undefined"},
    /* 40 */ {"var40", "undefined"},
    /* 41 */ {"var41", "undefined"},
    /* 42 */ {"var42", "undefined"},
    /* 43 */ {"var43", "undefined"},
    /* 44 */ {"var44", "undefined"},
    /* 45 */ {"var45", "undefined"},
    /* 46 */ {"var46", "undefined"},
    /* 47 */ {"var47", "undefined"},
    /* 48 */ {"var48", "undefined"},
    /* 49 */ {"var49", "undefined"},
    /* 50 */ {"var50", "undefined"},
    /* 51 */ {"var51", "undefined"},
    /* 52 */ {"var52", "undefined"},
    /* 53 */ {"var53", "undefined"},
    /* 54 */ {"var54", "undefined"},
    /* 55 */ {"var55", "undefined"},
    /* 56 */ {"var56", "undefined"},
    /* 57 */ {"var57", "undefined"},
    /* 58 */ {"var58", "undefined"},
    /* 59 */ {"var59", "undefined"},
    /* 60 */ {"var60", "undefined"},
    /* 61 */ {"var61", "undefined"},
    /* 62 */ {"var62", "undefined"},
    /* 63 */ {"var63", "undefined"},
    /* 64 */ {"var64", "undefined"},
    /* 65 */ {"var65", "undefined"},
    /* 66 */ {"var66", "undefined"},
    /* 67 */ {"var67", "undefined"},
    /* 68 */ {"var68", "undefined"},
    /* 69 */ {"var69", "undefined"},
    /* 70 */ {"var70", "undefined"},
    /* 71 */ {"var71", "undefined"},
    /* 72 */ {"var72", "undefined"},
    /* 73 */ {"var73", "undefined"},
    /* 74 */ {"var74", "undefined"},
    /* 75 */ {"var75", "undefined"},
    /* 76 */ {"var76", "undefined"},
    /* 77 */ {"var77", "undefined"},
    /* 78 */ {"var78", "undefined"},
    /* 79 */ {"var79", "undefined"},
    /* 80 */ {"var80", "undefined"},
    /* 81 */ {"var81", "undefined"},
    /* 82 */ {"var82", "undefined"},
    /* 83 */ {"var83", "undefined"},
    /* 84 */ {"var84", "undefined"},
    /* 85 */ {"var85", "undefined"},
    /* 86 */ {"var86", "undefined"},
    /* 87 */ {"var87", "undefined"},
    /* 88 */ {"var88", "undefined"},
    /* 89 */ {"var89", "undefined"},
    /* 90 */ {"var90", "undefined"},
    /* 91 */ {"var91", "undefined"},
    /* 92 */ {"var92", "undefined"},
    /* 93 */ {"var93", "undefined"},
    /* 94 */ {"var94", "undefined"},
    /* 95 */ {"var95", "undefined"},
    /* 96 */ {"var96", "undefined"},
    /* 97 */ {"var97", "undefined"},
    /* 98 */ {"var98", "undefined"},
    /* 99 */ {"var99", "undefined"},
    /* 100 */ {"var100", "undefined"},
    /* 101 */ {"var101", "undefined"},
    /* 102 */ {"var102", "undefined"},
    /* 103 */ {"var103", "undefined"},
    /* 104 */ {"var104", "undefined"},
    /* 105 */ {"var105", "undefined"},
    /* 106 */ {"var106", "undefined"},
    /* 107 */ {"var107", "undefined"},
    /* 108 */ {"var108", "undefined"},
    /* 109 */ {"var109", "undefined"},
    /* 110 */ {"var110", "undefined"},
    /* 111 */ {"var111", "undefined"},
    /* 112 */ {"var112", "undefined"},
    /* 113 */ {"var113", "undefined"},
    /* 114 */ {"var114", "undefined"},
    /* 115 */ {"var115", "undefined"},
    /* 116 */ {"var116", "undefined"},
    /* 117 */ {"var117", "undefined"},
    /* 118 */ {"var118", "undefined"},
    /* 119 */ {"var119", "undefined"},
    /* 120 */ {"var120", "undefined"},
    /* 121 */ {"var121", "undefined"},
    /* 122 */ {"var122", "undefined"},
    /* 123 */ {"var123", "undefined"},
    /* 124 */ {"var124", "undefined"},
    /* 125 */ {"var125", "undefined"},
    /* 126 */ {"var126", "undefined"},
    /* 127 */ {"var127", "undefined"},
    /* 128 */ {"var128", "undefined"},
    /* 129 */ {"var129", "undefined"},
    /* 130 */ {"var130", "undefined"},
    /* 131 */ {"var131", "undefined"},
    /* 132 */ {"var132", "undefined"},
    /* 133 */ {"var133", "undefined"},
    /* 134 */ {"var134", "undefined"},
    /* 135 */ {"var135", "undefined"},
    /* 136 */ {"var136", "undefined"},
    /* 137 */ {"var137", "undefined"},
    /* 138 */ {"var138", "undefined"},
    /* 139 */ {"var139", "undefined"},
    /* 140 */ {"var140", "undefined"},
    /* 141 */ {"var141", "undefined"},
    /* 142 */ {"var142", "undefined"},
    /* 143 */ {"var143", "undefined"},
    /* 144 */ {"var144", "undefined"},
    /* 145 */ {"var145", "undefined"},
    /* 146 */ {"var146", "undefined"},
    /* 147 */ {"var147", "undefined"},
    /* 148 */ {"var148", "undefined"},
    /* 149 */ {"var149", "undefined"},
    /* 150 */ {"var150", "undefined"},
    /* 151 */ {"var151", "undefined"},
    /* 152 */ {"var152", "undefined"},
    /* 153 */ {"var153", "undefined"},
    /* 154 */ {"var154", "undefined"},
    /* 155 */ {"var155", "undefined"},
    /* 156 */ {"var156", "undefined"},
    /* 157 */ {"var157", "undefined"},
    /* 158 */ {"var158", "undefined"},
    /* 159 */ {"var159", "undefined"},
    /* 160 */ {"var160", "undefined"},
    /* 161 */ {"var161", "undefined"},
    /* 162 */ {"var162", "undefined"},
    /* 163 */ {"var163", "undefined"},
    /* 164 */ {"var164", "undefined"},
    /* 165 */ {"var165", "undefined"},
    /* 166 */ {"var166", "undefined"},
    /* 167 */ {"var167", "undefined"},
    /* 168 */ {"var168", "undefined"},
    /* 169 */ {"var169", "undefined"},
    /* 170 */ {"var170", "undefined"},
    /* 171 */ {"var171", "undefined"},
    /* 172 */ {"var172", "undefined"},
    /* 173 */ {"var173", "undefined"},
    /* 174 */ {"var174", "undefined"},
    /* 175 */ {"var175", "undefined"},
    /* 176 */ {"var176", "undefined"},
    /* 177 */ {"var177", "undefined"},
    /* 178 */ {"var178", "undefined"},
    /* 179 */ {"var179", "undefined"},
    /* 180 */ {"var180", "undefined"},
    /* 181 */ {"var181", "undefined"},
    /* 182 */ {"var182", "undefined"},
    /* 183 */ {"var183", "undefined"},
    /* 184 */ {"var184", "undefined"},
    /* 185 */ {"var185", "undefined"},
    /* 186 */ {"var186", "undefined"},
    /* 187 */ {"var187", "undefined"},
    /* 188 */ {"var188", "undefined"},
    /* 189 */ {"var189", "undefined"},
    /* 190 */ {"var190", "undefined"},
    /* 191 */ {"var191", "undefined"},
    /* 192 */ {"var192", "undefined"},
    /* 193 */ {"var193", "undefined"},
    /* 194 */ {"var194", "undefined"},
    /* 195 */ {"var195", "undefined"},
    /* 196 */ {"var196", "undefined"},
    /* 197 */ {"var197", "undefined"},
    /* 198 */ {"var198", "undefined"},
    /* 199 */ {"var199", "undefined"},
    /* 200 */ {"var200", "undefined"},
    /* 201 */ {"var201", "undefined"},
    /* 202 */ {"var202", "undefined"},
    /* 203 */ {"var203", "undefined"},
    /* 204 */ {"var204", "undefined"},
    /* 205 */ {"var205", "undefined"},
    /* 206 */ {"var206", "undefined"},
    /* 207 */ {"var207", "undefined"},
    /* 208 */ {"TSRU", "Top solar radiation upward W m**-2"},
    /* 209 */ {"TTRU", "Top thermal radiation upward W m**-2"},
    /* 210 */ {"TSUC", "Top solar radiation upward clear sky W m**-2"},
    /* 211 */ {"TTUC", "Top thermal radiation upward clear sky W m**-2"},
    /* 212 */ {"CLW", "Cloud liquid water kg kg**-1"},
    /* 213 */ {"CF", "Cloud fraction 0-1"},
    /* 214 */ {"DHR", "Diabatic heating by radiation K s**-1"},
    /* 215 */ {"DHVD", "Diabatic heating by vertical diffusion K s**-1"},
    /* 216 */ {"DHCC", "Diabatic heating by cumulus convection K s**-1"},
    /* 217 */ {"DHLC", "Diabatic heating by large-scale condensation K s**-1"},
    /* 218 */ {"VDZW", "Vertical diffusion of zonal wind m**2 s**-3"},
    /* 219 */ {"VDMW", "Vertical diffusion of meridional wind m**2 s**-3"},
    /* 220 */ {"EWGD", "EW gravity wave drag m**2 s**-3"},
    /* 221 */ {"NSGD", "NS gravity wave drag m**2 s**-3"},
    /* 222 */ {"CTZW", "Convective tendency of zonal wind m**2 s**-3"},
    /* 223 */ {"CTMW", "Convective tendency of meridional wind m**2 s**-3"},
    /* 224 */ {"VDH", "Vertical diffusion of humidity kg kg**-1 s**-1"},
    /* 225 */ {"HTCC", "Humidity tendency by cumulus convection kg kg**-1 s**-1"},
    /* 226 */ {"HTLC", "Humidity tendency by large-scale condensation kg kg**-1 s**-1"},
    /* 227 */ {"CRNH", "Change from removing negative humidity kg kg**-1 s**-1"},
    /* 228 */ {"ATT", "Adiabatic tendency of temperature K s**-1"},
    /* 229 */ {"ATH", "Adiabatic tendency of humidity kg kg**-1 s**-1"},
    /* 230 */ {"ATZW", "Adiabatic tendency of zonal wind m**2 s**-3"},
    /* 231 */ {"ATMW", "Adiabatic tendency of meridional wind m**2 s**-3"},
    /* 232 */ {"MVV", "Mean vertical velocity Pa s**-1"},
    /* 233 */ {"var233", "undefined"},
    /* 234 */ {"var234", "undefined"},
    /* 235 */ {"var235", "undefined"},
    /* 236 */ {"var236", "undefined"},
    /* 237 */ {"var237", "undefined"},
    /* 238 */ {"var238", "undefined"},
    /* 239 */ {"var239", "undefined"},
    /* 240 */ {"var240", "undefined"},
    /* 241 */ {"var241", "undefined"},
    /* 242 */ {"var242", "undefined"},
    /* 243 */ {"var243", "undefined"},
    /* 244 */ {"var244", "undefined"},
    /* 245 */ {"var245", "undefined"},
    /* 246 */ {"var246", "undefined"},
    /* 247 */ {"var247", "undefined"},
    /* 248 */ {"var248", "undefined"},
    /* 249 */ {"var249", "undefined"},
    /* 250 */ {"var250", "undefined"},
    /* 251 */ {"var251", "undefined"},
    /* 252 */ {"var252", "undefined"},
    /* 253 */ {"var253", "undefined"},
    /* 254 */ {"var254", "undefined"},
    /* 255 */ {"var255", "undefined"},
};


struct ParmTable parm_table_ecmwf_131[256] = {
    /* 0 */ {"var0", "undefined"},
    /* 1 */ {"var1", "undefined"},
    /* 2 */ {"var2", "undefined"},
    /* 3 */ {"var3", "undefined"},
    /* 4 */ {"var4", "undefined"},
    /* 5 */ {"var5", "undefined"},
    /* 6 */ {"var6", "undefined"},
    /* 7 */ {"var7", "undefined"},
    /* 8 */ {"var8", "undefined"},
    /* 9 */ {"var9", "undefined"},
    /* 10 */ {"var10", "undefined"},
    /* 11 */ {"var11", "undefined"},
    /* 12 */ {"var12", "undefined"},
    /* 13 */ {"var13", "undefined"},
    /* 14 */ {"var14", "undefined"},
    /* 15 */ {"var15", "undefined"},
    /* 16 */ {"var16", "undefined"},
    /* 17 */ {"var17", "undefined"},
    /* 18 */ {"var18", "undefined"},
    /* 19 */ {"var19", "undefined"},
    /* 20 */ {"var20", "undefined"},
    /* 21 */ {"var21", "undefined"},
    /* 22 */ {"var22", "undefined"},
    /* 23 */ {"var23", "undefined"},
    /* 24 */ {"var24", "undefined"},
    /* 25 */ {"var25", "undefined"},
    /* 26 */ {"var26", "undefined"},
    /* 27 */ {"var27", "undefined"},
    /* 28 */ {"var28", "undefined"},
    /* 29 */ {"var29", "undefined"},
    /* 30 */ {"var30", "undefined"},
    /* 31 */ {"var31", "undefined"},
    /* 32 */ {"var32", "undefined"},
    /* 33 */ {"var33", "undefined"},
    /* 34 */ {"var34", "undefined"},
    /* 35 */ {"var35", "undefined"},
    /* 36 */ {"var36", "undefined"},
    /* 37 */ {"var37", "undefined"},
    /* 38 */ {"var38", "undefined"},
    /* 39 */ {"var39", "undefined"},
    /* 40 */ {"var40", "undefined"},
    /* 41 */ {"var41", "undefined"},
    /* 42 */ {"var42", "undefined"},
    /* 43 */ {"var43", "undefined"},
    /* 44 */ {"var44", "undefined"},
    /* 45 */ {"var45", "undefined"},
    /* 46 */ {"var46", "undefined"},
    /* 47 */ {"var47", "undefined"},
    /* 48 */ {"var48", "undefined"},
    /* 49 */ {"var49", "undefined"},
    /* 50 */ {"var50", "undefined"},
    /* 51 */ {"var51", "undefined"},
    /* 52 */ {"var52", "undefined"},
    /* 53 */ {"var53", "undefined"},
    /* 54 */ {"var54", "undefined"},
    /* 55 */ {"var55", "undefined"},
    /* 56 */ {"var56", "undefined"},
    /* 57 */ {"var57", "undefined"},
    /* 58 */ {"var58", "undefined"},
    /* 59 */ {"var59", "undefined"},
    /* 60 */ {"var60", "undefined"},
    /* 61 */ {"var61", "undefined"},
    /* 62 */ {"var62", "undefined"},
    /* 63 */ {"var63", "undefined"},
    /* 64 */ {"var64", "undefined"},
    /* 65 */ {"var65", "undefined"},
    /* 66 */ {"var66", "undefined"},
    /* 67 */ {"var67", "undefined"},
    /* 68 */ {"var68", "undefined"},
    /* 69 */ {"var69", "undefined"},
    /* 70 */ {"var70", "undefined"},
    /* 71 */ {"var71", "undefined"},
    /* 72 */ {"var72", "undefined"},
    /* 73 */ {"var73", "undefined"},
    /* 74 */ {"var74", "undefined"},
    /* 75 */ {"var75", "undefined"},
    /* 76 */ {"var76", "undefined"},
    /* 77 */ {"var77", "undefined"},
    /* 78 */ {"var78", "undefined"},
    /* 79 */ {"var79", "undefined"},
    /* 80 */ {"var80", "undefined"},
    /* 81 */ {"var81", "undefined"},
    /* 82 */ {"var82", "undefined"},
    /* 83 */ {"var83", "undefined"},
    /* 84 */ {"var84", "undefined"},
    /* 85 */ {"var85", "undefined"},
    /* 86 */ {"var86", "undefined"},
    /* 87 */ {"var87", "undefined"},
    /* 88 */ {"var88", "undefined"},
    /* 89 */ {"var89", "undefined"},
    /* 90 */ {"var90", "undefined"},
    /* 91 */ {"var91", "undefined"},
    /* 92 */ {"var92", "undefined"},
    /* 93 */ {"var93", "undefined"},
    /* 94 */ {"var94", "undefined"},
    /* 95 */ {"var95", "undefined"},
    /* 96 */ {"var96", "undefined"},
    /* 97 */ {"var97", "undefined"},
    /* 98 */ {"var98", "undefined"},
    /* 99 */ {"var99", "undefined"},
    /* 100 */ {"var100", "undefined"},
    /* 101 */ {"var101", "undefined"},
    /* 102 */ {"var102", "undefined"},
    /* 103 */ {"var103", "undefined"},
    /* 104 */ {"var104", "undefined"},
    /* 105 */ {"var105", "undefined"},
    /* 106 */ {"var106", "undefined"},
    /* 107 */ {"var107", "undefined"},
    /* 108 */ {"var108", "undefined"},
    /* 109 */ {"var109", "undefined"},
    /* 110 */ {"var110", "undefined"},
    /* 111 */ {"var111", "undefined"},
    /* 112 */ {"var112", "undefined"},
    /* 113 */ {"var113", "undefined"},
    /* 114 */ {"var114", "undefined"},
    /* 115 */ {"var115", "undefined"},
    /* 116 */ {"var116", "undefined"},
    /* 117 */ {"var117", "undefined"},
    /* 118 */ {"var118", "undefined"},
    /* 119 */ {"var119", "undefined"},
    /* 120 */ {"var120", "undefined"},
    /* 121 */ {"var121", "undefined"},
    /* 122 */ {"var122", "undefined"},
    /* 123 */ {"var123", "undefined"},
    /* 124 */ {"var124", "undefined"},
    /* 125 */ {"var125", "undefined"},
    /* 126 */ {"var126", "undefined"},
    /* 127 */ {"var127", "undefined"},
    /* 128 */ {"var128", "undefined"},
    /* 129 */ {"var129", "undefined"},
    /* 130 */ {"TAP", "Temperature anomaly probability % K"},
    /* 131 */ {"var131", "undefined"},
    /* 132 */ {"var132", "undefined"},
    /* 133 */ {"var133", "undefined"},
    /* 134 */ {"var134", "undefined"},
    /* 135 */ {"var135", "undefined"},
    /* 136 */ {"var136", "undefined"},
    /* 137 */ {"var137", "undefined"},
    /* 138 */ {"var138", "undefined"},
    /* 139 */ {"var139", "undefined"},
    /* 140 */ {"var140", "undefined"},
    /* 141 */ {"var141", "undefined"},
    /* 142 */ {"var142", "undefined"},
    /* 143 */ {"var143", "undefined"},
    /* 144 */ {"var144", "undefined"},
    /* 145 */ {"var145", "undefined"},
    /* 146 */ {"var146", "undefined"},
    /* 147 */ {"var147", "undefined"},
    /* 148 */ {"var148", "undefined"},
    /* 149 */ {"var149", "undefined"},
    /* 150 */ {"var150", "undefined"},
    /* 151 */ {"var151", "undefined"},
    /* 152 */ {"var152", "undefined"},
    /* 153 */ {"var153", "undefined"},
    /* 154 */ {"var154", "undefined"},
    /* 155 */ {"var155", "undefined"},
    /* 156 */ {"var156", "undefined"},
    /* 157 */ {"var157", "undefined"},
    /* 158 */ {"var158", "undefined"},
    /* 159 */ {"var159", "undefined"},
    /* 160 */ {"var160", "undefined"},
    /* 161 */ {"var161", "undefined"},
    /* 162 */ {"var162", "undefined"},
    /* 163 */ {"var163", "undefined"},
    /* 164 */ {"var164", "undefined"},
    /* 165 */ {"10SP", "10 metre speed probability % m s**-1"},
    /* 166 */ {"var166", "undefined"},
    /* 167 */ {"2TP", "2 metre temperature probability %"},
    /* 168 */ {"var168", "undefined"},
    /* 169 */ {"var169", "undefined"},
    /* 170 */ {"var170", "undefined"},
    /* 171 */ {"var171", "undefined"},
    /* 172 */ {"var172", "undefined"},
    /* 173 */ {"var173", "undefined"},
    /* 174 */ {"var174", "undefined"},
    /* 175 */ {"var175", "undefined"},
    /* 176 */ {"var176", "undefined"},
    /* 177 */ {"var177", "undefined"},
    /* 178 */ {"var178", "undefined"},
    /* 179 */ {"var179", "undefined"},
    /* 180 */ {"var180", "undefined"},
    /* 181 */ {"var181", "undefined"},
    /* 182 */ {"var182", "undefined"},
    /* 183 */ {"var183", "undefined"},
    /* 184 */ {"var184", "undefined"},
    /* 185 */ {"var185", "undefined"},
    /* 186 */ {"var186", "undefined"},
    /* 187 */ {"var187", "undefined"},
    /* 188 */ {"var188", "undefined"},
    /* 189 */ {"var189", "undefined"},
    /* 190 */ {"var190", "undefined"},
    /* 191 */ {"var191", "undefined"},
    /* 192 */ {"var192", "undefined"},
    /* 193 */ {"var193", "undefined"},
    /* 194 */ {"var194", "undefined"},
    /* 195 */ {"var195", "undefined"},
    /* 196 */ {"var196", "undefined"},
    /* 197 */ {"var197", "undefined"},
    /* 198 */ {"var198", "undefined"},
    /* 199 */ {"var199", "undefined"},
    /* 200 */ {"var200", "undefined"},
    /* 201 */ {"var201", "undefined"},
    /* 202 */ {"var202", "undefined"},
    /* 203 */ {"var203", "undefined"},
    /* 204 */ {"var204", "undefined"},
    /* 205 */ {"var205", "undefined"},
    /* 206 */ {"var206", "undefined"},
    /* 207 */ {"var207", "undefined"},
    /* 208 */ {"var208", "undefined"},
    /* 209 */ {"var209", "undefined"},
    /* 210 */ {"var210", "undefined"},
    /* 211 */ {"var211", "undefined"},
    /* 212 */ {"var212", "undefined"},
    /* 213 */ {"var213", "undefined"},
    /* 214 */ {"var214", "undefined"},
    /* 215 */ {"var215", "undefined"},
    /* 216 */ {"var216", "undefined"},
    /* 217 */ {"var217", "undefined"},
    /* 218 */ {"var218", "undefined"},
    /* 219 */ {"var219", "undefined"},
    /* 220 */ {"var220", "undefined"},
    /* 221 */ {"var221", "undefined"},
    /* 222 */ {"var222", "undefined"},
    /* 223 */ {"var223", "undefined"},
    /* 224 */ {"var224", "undefined"},
    /* 225 */ {"var225", "undefined"},
    /* 226 */ {"var226", "undefined"},
    /* 227 */ {"var227", "undefined"},
    /* 228 */ {"TPP", "Total precipitation probability % m"},
    /* 229 */ {"var229", "undefined"},
    /* 230 */ {"var230", "undefined"},
    /* 231 */ {"var231", "undefined"},
    /* 232 */ {"var232", "undefined"},
    /* 233 */ {"var233", "undefined"},
    /* 234 */ {"var234", "undefined"},
    /* 235 */ {"var235", "undefined"},
    /* 236 */ {"var236", "undefined"},
    /* 237 */ {"var237", "undefined"},
    /* 238 */ {"var238", "undefined"},
    /* 239 */ {"var239", "undefined"},
    /* 240 */ {"var240", "undefined"},
    /* 241 */ {"var241", "undefined"},
    /* 242 */ {"var242", "undefined"},
    /* 243 */ {"var243", "undefined"},
    /* 244 */ {"var244", "undefined"},
    /* 245 */ {"var245", "undefined"},
    /* 246 */ {"var246", "undefined"},
    /* 247 */ {"var247", "undefined"},
    /* 248 */ {"var248", "undefined"},
    /* 249 */ {"var249", "undefined"},
    /* 250 */ {"var250", "undefined"},
    /* 251 */ {"var251", "undefined"},
    /* 252 */ {"var252", "undefined"},
    /* 253 */ {"var253", "undefined"},
    /* 254 */ {"var254", "undefined"},
    /* 255 */ {"var255", "undefined"},
};

struct ParmTable parm_table_ecmwf_140[256] = {
      /* 0 */ {"var0", "undefined"},
      /* 1 */ {"var1", "undefined"},
      /* 2 */ {"var2", "undefined"},
      /* 3 */ {"var3", "undefined"},
      /* 4 */ {"var4", "undefined"},
      /* 5 */ {"var5", "undefined"},
      /* 6 */ {"var6", "undefined"},
      /* 7 */ {"var7", "undefined"},
      /* 8 */ {"var8", "undefined"},
      /* 9 */ {"var9", "undefined"},
      /* 10 */ {"var10", "undefined"},
      /* 11 */ {"var11", "undefined"},
      /* 12 */ {"var12", "undefined"},
      /* 13 */ {"var13", "undefined"},
      /* 14 */ {"var14", "undefined"},
      /* 15 */ {"var15", "undefined"},
      /* 16 */ {"var16", "undefined"},
      /* 17 */ {"var17", "undefined"},
      /* 18 */ {"var18", "undefined"},
      /* 19 */ {"var19", "undefined"},
      /* 20 */ {"var20", "undefined"},
      /* 21 */ {"var21", "undefined"},
      /* 22 */ {"var22", "undefined"},
      /* 23 */ {"var23", "undefined"},
      /* 24 */ {"var24", "undefined"},
      /* 25 */ {"var25", "undefined"},
      /* 26 */ {"var26", "undefined"},
      /* 27 */ {"var27", "undefined"},
      /* 28 */ {"var28", "undefined"},
      /* 29 */ {"var29", "undefined"},
      /* 30 */ {"var30", "undefined"},
      /* 31 */ {"var31", "undefined"},
      /* 32 */ {"var32", "undefined"},
      /* 33 */ {"var33", "undefined"},
      /* 34 */ {"var34", "undefined"},
      /* 35 */ {"var35", "undefined"},
      /* 36 */ {"var36", "undefined"},
      /* 37 */ {"var37", "undefined"},
      /* 38 */ {"var38", "undefined"},
      /* 39 */ {"var39", "undefined"},
      /* 40 */ {"var40", "undefined"},
      /* 41 */ {"var41", "undefined"},
      /* 42 */ {"var42", "undefined"},
      /* 43 */ {"var43", "undefined"},
      /* 44 */ {"var44", "undefined"},
      /* 45 */ {"var45", "undefined"},
      /* 46 */ {"var46", "undefined"},
      /* 47 */ {"var47", "undefined"},
      /* 48 */ {"var48", "undefined"},
      /* 49 */ {"var49", "undefined"},
      /* 50 */ {"var50", "undefined"},
      /* 51 */ {"var51", "undefined"},
      /* 52 */ {"var52", "undefined"},
      /* 53 */ {"var53", "undefined"},
      /* 54 */ {"var54", "undefined"},
      /* 55 */ {"var55", "undefined"},
      /* 56 */ {"var56", "undefined"},
      /* 57 */ {"var57", "undefined"},
      /* 58 */ {"var58", "undefined"},
      /* 59 */ {"var59", "undefined"},
      /* 60 */ {"var60", "undefined"},
      /* 61 */ {"var61", "undefined"},
      /* 62 */ {"var62", "undefined"},
      /* 63 */ {"var63", "undefined"},
      /* 64 */ {"var64", "undefined"},
      /* 65 */ {"var65", "undefined"},
      /* 66 */ {"var66", "undefined"},
      /* 67 */ {"var67", "undefined"},
      /* 68 */ {"var68", "undefined"},
      /* 69 */ {"var69", "undefined"},
      /* 70 */ {"var70", "undefined"},
      /* 71 */ {"var71", "undefined"},
      /* 72 */ {"var72", "undefined"},
      /* 73 */ {"var73", "undefined"},
      /* 74 */ {"var74", "undefined"},
      /* 75 */ {"var75", "undefined"},
      /* 76 */ {"var76", "undefined"},
      /* 77 */ {"var77", "undefined"},
      /* 78 */ {"var78", "undefined"},
      /* 79 */ {"var79", "undefined"},
      /* 80 */ {"var80", "undefined"},
      /* 81 */ {"var81", "undefined"},
      /* 82 */ {"var82", "undefined"},
      /* 83 */ {"var83", "undefined"},
      /* 84 */ {"var84", "undefined"},
      /* 85 */ {"var85", "undefined"},
      /* 86 */ {"var86", "undefined"},
      /* 87 */ {"var87", "undefined"},
      /* 88 */ {"var88", "undefined"},
      /* 89 */ {"var89", "undefined"},
      /* 90 */ {"var90", "undefined"},
      /* 91 */ {"var91", "undefined"},
      /* 92 */ {"var92", "undefined"},
      /* 93 */ {"var93", "undefined"},
      /* 94 */ {"var94", "undefined"},
      /* 95 */ {"var95", "undefined"},
      /* 96 */ {"var96", "undefined"},
      /* 97 */ {"var97", "undefined"},
      /* 98 */ {"var98", "undefined"},
      /* 99 */ {"var99", "undefined"},
      /* 100 */ {"var100", "undefined"},
      /* 101 */ {"var101", "undefined"},
      /* 102 */ {"var102", "undefined"},
      /* 103 */ {"var103", "undefined"},
      /* 104 */ {"var104", "undefined"},
      /* 105 */ {"var105", "undefined"},
      /* 106 */ {"var106", "undefined"},
      /* 107 */ {"var107", "undefined"},
      /* 108 */ {"var108", "undefined"},
      /* 109 */ {"var109", "undefined"},
      /* 110 */ {"var110", "undefined"},
      /* 111 */ {"var111", "undefined"},
      /* 112 */ {"var112", "undefined"},
      /* 113 */ {"var113", "undefined"},
      /* 114 */ {"var114", "undefined"},
      /* 115 */ {"var115", "undefined"},
      /* 116 */ {"var116", "undefined"},
      /* 117 */ {"var117", "undefined"},
      /* 118 */ {"var118", "undefined"},
      /* 119 */ {"var119", "undefined"},
      /* 120 */ {"var120", "undefined"},
      /* 121 */ {"var121", "undefined"},
      /* 122 */ {"var122", "undefined"},
      /* 123 */ {"var123", "undefined"},
      /* 124 */ {"var124", "undefined"},
      /* 125 */ {"var125", "undefined"},
      /* 126 */ {"var126", "undefined"},
      /* 127 */ {"var127", "undefined"},
      /* 128 */ {"var128", "undefined"},
      /* 129 */ {"var129", "undefined"},
      /* 130 */ {"var130", "undefined"},
      /* 131 */ {"var131", "undefined"},
      /* 132 */ {"var132", "undefined"},
      /* 133 */ {"var133", "undefined"},
      /* 134 */ {"var134", "undefined"},
      /* 135 */ {"var135", "undefined"},
      /* 136 */ {"var136", "undefined"},
      /* 137 */ {"var137", "undefined"},
      /* 138 */ {"var138", "undefined"},
      /* 139 */ {"var139", "undefined"},
      /* 140 */ {"var140", "undefined"},
      /* 141 */ {"var141", "undefined"},
      /* 142 */ {"var142", "undefined"},
      /* 143 */ {"var143", "undefined"},
      /* 144 */ {"var144", "undefined"},
      /* 145 */ {"var145", "undefined"},
      /* 146 */ {"var146", "undefined"},
      /* 147 */ {"var147", "undefined"},
      /* 148 */ {"var148", "undefined"},
      /* 149 */ {"var149", "undefined"},
      /* 150 */ {"var150", "undefined"},
      /* 151 */ {"var151", "undefined"},
      /* 152 */ {"var152", "undefined"},
      /* 153 */ {"var153", "undefined"},
      /* 154 */ {"var154", "undefined"},
      /* 155 */ {"var155", "undefined"},
      /* 156 */ {"var156", "undefined"},
      /* 157 */ {"var157", "undefined"},
      /* 158 */ {"var158", "undefined"},
      /* 159 */ {"var159", "undefined"},
      /* 160 */ {"var160", "undefined"},
      /* 161 */ {"var161", "undefined"},
      /* 162 */ {"var162", "undefined"},
      /* 163 */ {"var163", "undefined"},
      /* 164 */ {"var164", "undefined"},
      /* 165 */ {"var165", "undefined"},
      /* 166 */ {"var166", "undefined"},
      /* 167 */ {"var167", "undefined"},
      /* 168 */ {"var168", "undefined"},
      /* 169 */ {"var169", "undefined"},
      /* 170 */ {"var170", "undefined"},
      /* 171 */ {"var171", "undefined"},
      /* 172 */ {"var172", "undefined"},
      /* 173 */ {"var173", "undefined"},
      /* 174 */ {"var174", "undefined"},
      /* 175 */ {"var175", "undefined"},
      /* 176 */ {"var176", "undefined"},
      /* 177 */ {"var177", "undefined"},
      /* 178 */ {"var178", "undefined"},
      /* 179 */ {"var179", "undefined"},
      /* 180 */ {"var180", "undefined"},
      /* 181 */ {"var181", "undefined"},
      /* 182 */ {"var182", "undefined"},
      /* 183 */ {"var183", "undefined"},
      /* 184 */ {"var184", "undefined"},
      /* 185 */ {"var185", "undefined"},
      /* 186 */ {"var186", "undefined"},
      /* 187 */ {"var187", "undefined"},
      /* 188 */ {"var188", "undefined"},
      /* 189 */ {"var189", "undefined"},
      /* 190 */ {"var190", "undefined"},
      /* 191 */ {"var191", "undefined"},
      /* 192 */ {"var192", "undefined"},
      /* 193 */ {"var193", "undefined"},
      /* 194 */ {"var194", "undefined"},
      /* 195 */ {"var195", "undefined"},
      /* 196 */ {"var196", "undefined"},
      /* 197 */ {"var197", "undefined"},
      /* 198 */ {"var198", "undefined"},
      /* 199 */ {"var199", "undefined"},
      /* 200 */ {"var200", "undefined"},
      /* 201 */ {"var201", "undefined"},
      /* 202 */ {"var202", "undefined"},
      /* 203 */ {"var203", "undefined"},
      /* 204 */ {"var204", "undefined"},
      /* 205 */ {"var205", "undefined"},
      /* 206 */ {"var206", "undefined"},
      /* 207 */ {"var207", "undefined"},
      /* 208 */ {"var208", "undefined"},
      /* 209 */ {"var209", "undefined"},
      /* 210 */ {"var210", "undefined"},
      /* 211 */ {"var211", "undefined"},
      /* 212 */ {"var212", "undefined"},
      /* 213 */ {"var213", "undefined"},
      /* 214 */ {"var214", "undefined"},
      /* 215 */ {"var215", "undefined"},
      /* 216 */ {"var216", "undefined"},
      /* 217 */ {"var217", "undefined"},
      /* 218 */ {"var218", "undefined"},
      /* 219 */ {"var219", "undefined"},
      /* 220 */ {"MP1", "Mean wave period based on first moment [s]"},
      /* 221 */ {"MP2", "Mean wave period based on second moment [s]"},
      /* 222 */ {"WDW", "Wave spectral directional width"},
      /* 223 */ {"P1WW", "Mean wave period based on first moment for wind waves [s]"},
      /* 224 */ {"P2WW", "Mean wave period based on second moment for wind waves [s]"},
      /* 225 */ {"DWWW", "Wave spectral directional width for wind waves"},
      /* 226 */ {"P1PS", "Mean wave period based on first moment for swell [s]"},
      /* 227 */ {"P2PS", "Mean wave period based on second moment for swell [s]"},
      /* 228 */ {"DWPS", "Wave spectral directional width for swell"},
      /* 229 */ {"SWH", "Significant wave height [m]"},
      /* 230 */ {"MWD", "Mean wave direction [degrees]"},
      /* 231 */ {"PP1D", "Peak period of 1D spectra [s]"},
      /* 232 */ {"MWP", "Mean wave period [s]"},
      /* 233 */ {"CDWW", "Coefficient of drag with waves"},
      /* 234 */ {"SHWW", "Significant height of wind waves [m]"},
      /* 235 */ {"MDWW", "Mean direction of wind waves [degrees]"},
      /* 236 */ {"MPWW", "Mean period of wind waves [s]"},
      /* 237 */ {"SHPS", "Significant height of primary swell [m]"},
      /* 238 */ {"MDPS", "Mean direction of primary swell [degrees]"},
      /* 239 */ {"MPPS", "Mean period of primary swell [s]"},
      /* 240 */ {"SDHS", "Standard deviation wave height [m]"},
      /* 241 */ {"MU10", "Mean of 10 metre windspeed [m s**-1]"},
      /* 242 */ {"MDWI", "Mean wind direction [degrees]"},
      /* 243 */ {"SDU", "Standard deviation of 10 metre wind speed [m s**-1]"},
      /* 244 */ {"MSQS", "Mean square slope of waves [dimensionless]"},
      /* 245 */ {"WIND", "10 metre wind speed [m s**-1]"},
      /* 246 */ {"AWH", "Altimeter wave height [m]"},
      /* 247 */ {"ACWH", "Altimeter corrected wave height [m]"},
      /* 248 */ {"ARRC", "Altimeter range relative correction"},
      /* 249 */ {"DWI", "10 metre wind direction [degrees]"},
      /* 250 */ {"2DSP", "2D wave spectra (multiple) [m**2 s]"},
      /* 251 */ {"2DFD", "2D wave spectra (single) [m**2 s]"},
      /* 252 */ {"var252", "undefined"},
      /* 253 */ {"var253", "undefined"},
      /* 254 */ {"var254", "undefined"},
      /* 255 */ {"var255", "undefined"},
};


struct ParmTable parm_table_ecmwf_150[256] = {
    /* 0 */ {"var0", "undefined"},
    /* 1 */ {"var1", "undefined"},
    /* 2 */ {"var2", "undefined"},
    /* 3 */ {"var3", "undefined"},
    /* 4 */ {"var4", "undefined"},
    /* 5 */ {"var5", "undefined"},
    /* 6 */ {"var6", "undefined"},
    /* 7 */ {"var7", "undefined"},
    /* 8 */ {"var8", "undefined"},
    /* 9 */ {"var9", "undefined"},
    /* 10 */ {"var10", "undefined"},
    /* 11 */ {"var11", "undefined"},
    /* 12 */ {"var12", "undefined"},
    /* 13 */ {"var13", "undefined"},
    /* 14 */ {"var14", "undefined"},
    /* 15 */ {"var15", "undefined"},
    /* 16 */ {"var16", "undefined"},
    /* 17 */ {"var17", "undefined"},
    /* 18 */ {"var18", "undefined"},
    /* 19 */ {"var19", "undefined"},
    /* 20 */ {"var20", "undefined"},
    /* 21 */ {"var21", "undefined"},
    /* 22 */ {"var22", "undefined"},
    /* 23 */ {"var23", "undefined"},
    /* 24 */ {"var24", "undefined"},
    /* 25 */ {"var25", "undefined"},
    /* 26 */ {"var26", "undefined"},
    /* 27 */ {"var27", "undefined"},
    /* 28 */ {"var28", "undefined"},
    /* 29 */ {"var29", "undefined"},
    /* 30 */ {"var30", "undefined"},
    /* 31 */ {"var31", "undefined"},
    /* 32 */ {"var32", "undefined"},
    /* 33 */ {"var33", "undefined"},
    /* 34 */ {"var34", "undefined"},
    /* 35 */ {"var35", "undefined"},
    /* 36 */ {"var36", "undefined"},
    /* 37 */ {"var37", "undefined"},
    /* 38 */ {"var38", "undefined"},
    /* 39 */ {"var39", "undefined"},
    /* 40 */ {"var40", "undefined"},
    /* 41 */ {"var41", "undefined"},
    /* 42 */ {"var42", "undefined"},
    /* 43 */ {"var43", "undefined"},
    /* 44 */ {"var44", "undefined"},
    /* 45 */ {"var45", "undefined"},
    /* 46 */ {"var46", "undefined"},
    /* 47 */ {"var47", "undefined"},
    /* 48 */ {"var48", "undefined"},
    /* 49 */ {"var49", "undefined"},
    /* 50 */ {"var50", "undefined"},
    /* 51 */ {"var51", "undefined"},
    /* 52 */ {"var52", "undefined"},
    /* 53 */ {"var53", "undefined"},
    /* 54 */ {"var54", "undefined"},
    /* 55 */ {"var55", "undefined"},
    /* 56 */ {"var56", "undefined"},
    /* 57 */ {"var57", "undefined"},
    /* 58 */ {"var58", "undefined"},
    /* 59 */ {"var59", "undefined"},
    /* 60 */ {"var60", "undefined"},
    /* 61 */ {"var61", "undefined"},
    /* 62 */ {"var62", "undefined"},
    /* 63 */ {"var63", "undefined"},
    /* 64 */ {"var64", "undefined"},
    /* 65 */ {"var65", "undefined"},
    /* 66 */ {"var66", "undefined"},
    /* 67 */ {"var67", "undefined"},
    /* 68 */ {"var68", "undefined"},
    /* 69 */ {"var69", "undefined"},
    /* 70 */ {"var70", "undefined"},
    /* 71 */ {"var71", "undefined"},
    /* 72 */ {"var72", "undefined"},
    /* 73 */ {"var73", "undefined"},
    /* 74 */ {"var74", "undefined"},
    /* 75 */ {"var75", "undefined"},
    /* 76 */ {"var76", "undefined"},
    /* 77 */ {"var77", "undefined"},
    /* 78 */ {"var78", "undefined"},
    /* 79 */ {"var79", "undefined"},
    /* 80 */ {"var80", "undefined"},
    /* 81 */ {"var81", "undefined"},
    /* 82 */ {"var82", "undefined"},
    /* 83 */ {"var83", "undefined"},
    /* 84 */ {"var84", "undefined"},
    /* 85 */ {"var85", "undefined"},
    /* 86 */ {"var86", "undefined"},
    /* 87 */ {"var87", "undefined"},
    /* 88 */ {"var88", "undefined"},
    /* 89 */ {"var89", "undefined"},
    /* 90 */ {"var90", "undefined"},
    /* 91 */ {"var91", "undefined"},
    /* 92 */ {"var92", "undefined"},
    /* 93 */ {"var93", "undefined"},
    /* 94 */ {"var94", "undefined"},
    /* 95 */ {"var95", "undefined"},
    /* 96 */ {"var96", "undefined"},
    /* 97 */ {"var97", "undefined"},
    /* 98 */ {"var98", "undefined"},
    /* 99 */ {"var99", "undefined"},
    /* 100 */ {"var100", "undefined"},
    /* 101 */ {"var101", "undefined"},
    /* 102 */ {"var102", "undefined"},
    /* 103 */ {"var103", "undefined"},
    /* 104 */ {"var104", "undefined"},
    /* 105 */ {"var105", "undefined"},
    /* 106 */ {"var106", "undefined"},
    /* 107 */ {"var107", "undefined"},
    /* 108 */ {"var108", "undefined"},
    /* 109 */ {"var109", "undefined"},
    /* 110 */ {"var110", "undefined"},
    /* 111 */ {"var111", "undefined"},
    /* 112 */ {"var112", "undefined"},
    /* 113 */ {"var113", "undefined"},
    /* 114 */ {"var114", "undefined"},
    /* 115 */ {"var115", "undefined"},
    /* 116 */ {"var116", "undefined"},
    /* 117 */ {"var117", "undefined"},
    /* 118 */ {"var118", "undefined"},
    /* 119 */ {"var119", "undefined"},
    /* 120 */ {"var120", "undefined"},
    /* 121 */ {"var121", "undefined"},
    /* 122 */ {"var122", "undefined"},
    /* 123 */ {"var123", "undefined"},
    /* 124 */ {"var124", "undefined"},
    /* 125 */ {"var125", "undefined"},
    /* 126 */ {"var126", "undefined"},
    /* 127 */ {"var127", "undefined"},
    /* 128 */ {"var128", "undefined"},
    /* 129 */ {"NONE", "Ocean potential temperature deg C"},
    /* 130 */ {"NONE", "Ocean salinity psu"},
    /* 131 */ {"NONE", "Ocean potential density(reference = surface) kg m**-3 -1000"},
    /* 132 */ {"var132", "undefined"},
    /* 133 */ {"NONE", "Ocean u velocity m s**-1"},
    /* 134 */ {"NONE", "Ocean v velocity m s**-1"},
    /* 135 */ {"NONE", "Ocean w velocity m s**-1"},
    /* 136 */ {"var136", "undefined"},
    /* 137 */ {"NONE", "Richardson number -"},
    /* 138 */ {"var138", "undefined"},
    /* 139 */ {"NONE", "u*v product m s**-2"},
    /* 140 */ {"NONE", "u*T product m s**-1 deg C"},
    /* 141 */ {"NONE", "v*T product m s**-1 deg C"},
    /* 142 */ {"NONE", "u*u product m s**-2"},
    /* 143 */ {"NONE", "v*v product m s**-2"},
    /* 144 */ {"NONE", "uv - u~v~ (u~ is time-mean of u) m s**-2"},
    /* 145 */ {"NONE", "uT - u~T~ m s**-1 deg C"},
    /* 146 */ {"NONE", "vT - v~T~ m s**-1 deg C"},
    /* 147 */ {"NONE", "uu - u~u~ m s**-2"},
    /* 148 */ {"NONE", "vv - v~v~ m s**-2"},
    /* 149 */ {"var149", "undefined"},
    /* 150 */ {"var150", "undefined"},
    /* 151 */ {"var151", "undefined"},
    /* 152 */ {"NONE", "Sea level (departure from geoid tides removed)"},
    /* 153 */ {"NONE", "Barotropic stream function -"},
    /* 154 */ {"NONE", "Mixed layer depth (Tcr=0.5 C for HOPE model) m"},
    /* 155 */ {"NONE", "Depth (eg of isothermal surface) m"},
    /* 156 */ {"var156", "undefined"},
    /* 157 */ {"var157", "undefined"},
    /* 158 */ {"var158", "undefined"},
    /* 159 */ {"var159", "undefined"},
    /* 160 */ {"var160", "undefined"},
    /* 161 */ {"var161", "undefined"},
    /* 162 */ {"var162", "undefined"},
    /* 163 */ {"var163", "undefined"},
    /* 164 */ {"var164", "undefined"},
    /* 165 */ {"var165", "undefined"},
    /* 166 */ {"var166", "undefined"},
    /* 167 */ {"var167", "undefined"},
    /* 168 */ {"NONE", "U-stress Pa"},
    /* 169 */ {"NONE", "V-stress Pa"},
    /* 170 */ {"NONE", "Turbulent Kinetic Energy input -"},
    /* 171 */ {"NONE", "Net surface heat flux (+ve = down) -"},
    /* 172 */ {"NONE", "Surface solar radiation -"},
    /* 173 */ {"NONE", "P-E -"},
    /* 174 */ {"var174", "undefined"},
    /* 175 */ {"var175", "undefined"},
    /* 176 */ {"var176", "undefined"},
    /* 177 */ {"var177", "undefined"},
    /* 178 */ {"var178", "undefined"},
    /* 179 */ {"var179", "undefined"},
    /* 180 */ {"NONE", "Diagnosed SST eror deg C"},
    /* 181 */ {"NONE", "Heat flux correction W m**-2"},
    /* 182 */ {"NONE", "Observed SST deg C"},
    /* 183 */ {"NONE", "Observed heat flux W m**-2"},
    /* 184 */ {"var184", "undefined"},
    /* 185 */ {"var185", "undefined"},
    /* 186 */ {"var186", "undefined"},
    /* 187 */ {"var187", "undefined"},
    /* 188 */ {"var188", "undefined"},
    /* 189 */ {"var189", "undefined"},
    /* 190 */ {"var190", "undefined"},
    /* 191 */ {"var191", "undefined"},
    /* 192 */ {"var192", "undefined"},
    /* 193 */ {"var193", "undefined"},
    /* 194 */ {"var194", "undefined"},
    /* 195 */ {"var195", "undefined"},
    /* 196 */ {"var196", "undefined"},
    /* 197 */ {"var197", "undefined"},
    /* 198 */ {"var198", "undefined"},
    /* 199 */ {"var199", "undefined"},
    /* 200 */ {"var200", "undefined"},
    /* 201 */ {"var201", "undefined"},
    /* 202 */ {"var202", "undefined"},
    /* 203 */ {"var203", "undefined"},
    /* 204 */ {"var204", "undefined"},
    /* 205 */ {"var205", "undefined"},
    /* 206 */ {"var206", "undefined"},
    /* 207 */ {"var207", "undefined"},
    /* 208 */ {"var208", "undefined"},
    /* 209 */ {"var209", "undefined"},
    /* 210 */ {"var210", "undefined"},
    /* 211 */ {"var211", "undefined"},
    /* 212 */ {"var212", "undefined"},
    /* 213 */ {"var213", "undefined"},
    /* 214 */ {"var214", "undefined"},
    /* 215 */ {"var215", "undefined"},
    /* 216 */ {"var216", "undefined"},
    /* 217 */ {"var217", "undefined"},
    /* 218 */ {"var218", "undefined"},
    /* 219 */ {"var219", "undefined"},
    /* 220 */ {"var220", "undefined"},
    /* 221 */ {"var221", "undefined"},
    /* 222 */ {"var222", "undefined"},
    /* 223 */ {"var223", "undefined"},
    /* 224 */ {"var224", "undefined"},
    /* 225 */ {"var225", "undefined"},
    /* 226 */ {"var226", "undefined"},
    /* 227 */ {"var227", "undefined"},
    /* 228 */ {"var228", "undefined"},
    /* 229 */ {"var229", "undefined"},
    /* 230 */ {"var230", "undefined"},
    /* 231 */ {"var231", "undefined"},
    /* 232 */ {"var232", "undefined"},
    /* 233 */ {"var233", "undefined"},
    /* 234 */ {"var234", "undefined"},
    /* 235 */ {"var235", "undefined"},
    /* 236 */ {"var236", "undefined"},
    /* 237 */ {"var237", "undefined"},
    /* 238 */ {"var238", "undefined"},
    /* 239 */ {"var239", "undefined"},
    /* 240 */ {"var240", "undefined"},
    /* 241 */ {"var241", "undefined"},
    /* 242 */ {"var242", "undefined"},
    /* 243 */ {"var243", "undefined"},
    /* 244 */ {"var244", "undefined"},
    /* 245 */ {"var245", "undefined"},
    /* 246 */ {"var246", "undefined"},
    /* 247 */ {"var247", "undefined"},
    /* 248 */ {"var248", "undefined"},
    /* 249 */ {"var249", "undefined"},
    /* 250 */ {"var250", "undefined"},
    /* 251 */ {"var251", "undefined"},
    /* 252 */ {"var252", "undefined"},
    /* 253 */ {"var253", "undefined"},
    /* 254 */ {"var254", "undefined"},
    /* 255 */ {"var255", "undefined"},
};

struct ParmTable parm_table_ecmwf_151[256] = {
      /* 0 */ {"var0", "undefined"},
      /* 1 */ {"T", "Accum. potential temperature deg C"},
      /* 2 */ {"", "Accum. salinity"},
      /* 3 */ {"", "Accum. U-velocity m s**-1"},
      /* 4 */ {"", "Accum. V-velocity m s**-1"},
      /* 5 */ {"V", "Accum. W-velocity m s**-1"},
      /* 6 */ {"ST", "Accum. modulus of strain rate tensor s**-1"},
      /* 7 */ {"VS", "Accum. vertical viscosity m**2 s**-1"},
      /* 8 */ {"DF", "Accum. vertical diffusivity m**2 s**-1"},
      /* 9 */ {"EP", "Accum. depth m"},
      /* 10 */ {"STH", "Accum. sigma-theta kg m**-3"},
      /* 11 */ {"RN", "Accum. Richardson number -"},
      /* 12 */ {"UV", "Accum. u*v product m**2 s**-2"},
      /* 13 */ {"UT", "Accum. u*T product m s**-1 deg C"},
      /* 14 */ {"VT", "Accum. v*T product m s**-1 deg C"},
      /* 15 */ {"UU", "Accum. u*u product m**2 s**-2"},
      /* 16 */ {"VV", "Accum. v*v product m**2 s**-2"},
      /* 17 */ {"SL", "Accum. sea level (tides removed) m"},
      /* 18 */ {"var18", "undefined"},
      /* 19 */ {"BSF", "Accum. barotropic streamfunction m**3 s**-1"},
      /* 20 */ {"MLD", "Accum. mixed layer depth m"},
      /* 21 */ {"var21", "undefined"},
      /* 22 */ {"var22", "undefined"},
      /* 23 */ {"var23", "undefined"},
      /* 24 */ {"var24", "undefined"},
      /* 25 */ {"TAX", "Accum. U-stress Pa"},
      /* 26 */ {"TAY", "Accum. V-stress Pa"},
      /* 27 */ {"TKI", "Accum. turbulent kinetic energy input W m**-2"},
      /* 28 */ {"NSF", "Accum. net surface heat flux W m**-2"},
      /* 29 */ {"ASR", "Accum. absorbed solar radiation W m**-2"},
      /* 30 */ {"PME", "Accum. precipitation - evaporation m s**-1"},
      /* 31 */ {"SST", "Accum. specified SST deg C"},
      /* 32 */ {"SHF", "Accum. specified surface heat flux W m**-2"},
      /* 33 */ {"DTE", "Accum. diagnosed SST error deg C"},
      /* 34 */ {"HFC", "Accum. heat flux correction W m**-2"},
      /* 35 */ {"var35", "undefined"},
      /* 36 */ {"var36", "undefined"},
      /* 37 */ {"var37", "undefined"},
      /* 38 */ {"var38", "undefined"},
      /* 39 */ {"var39", "undefined"},
      /* 40 */ {"var40", "undefined"},
      /* 41 */ {"var41", "undefined"},
      /* 42 */ {"var42", "undefined"},
      /* 43 */ {"var43", "undefined"},
      /* 44 */ {"var44", "undefined"},
      /* 45 */ {"var45", "undefined"},
      /* 46 */ {"var46", "undefined"},
      /* 47 */ {"var47", "undefined"},
      /* 48 */ {"var48", "undefined"},
      /* 49 */ {"var49", "undefined"},
      /* 50 */ {"var50", "undefined"},
      /* 51 */ {"var51", "undefined"},
      /* 52 */ {"var52", "undefined"},
      /* 53 */ {"var53", "undefined"},
      /* 54 */ {"var54", "undefined"},
      /* 55 */ {"var55", "undefined"},
      /* 56 */ {"var56", "undefined"},
      /* 57 */ {"var57", "undefined"},
      /* 58 */ {"var58", "undefined"},
      /* 59 */ {"var59", "undefined"},
      /* 60 */ {"var60", "undefined"},
      /* 61 */ {"var61", "undefined"},
      /* 62 */ {"var62", "undefined"},
      /* 63 */ {"var63", "undefined"},
      /* 64 */ {"var64", "undefined"},
      /* 65 */ {"var65", "undefined"},
      /* 66 */ {"var66", "undefined"},
      /* 67 */ {"var67", "undefined"},
      /* 68 */ {"var68", "undefined"},
      /* 69 */ {"var69", "undefined"},
      /* 70 */ {"var70", "undefined"},
      /* 71 */ {"var71", "undefined"},
      /* 72 */ {"var72", "undefined"},
      /* 73 */ {"var73", "undefined"},
      /* 74 */ {"var74", "undefined"},
      /* 75 */ {"var75", "undefined"},
      /* 76 */ {"var76", "undefined"},
      /* 77 */ {"var77", "undefined"},
      /* 78 */ {"var78", "undefined"},
      /* 79 */ {"var79", "undefined"},
      /* 80 */ {"var80", "undefined"},
      /* 81 */ {"var81", "undefined"},
      /* 82 */ {"var82", "undefined"},
      /* 83 */ {"var83", "undefined"},
      /* 84 */ {"var84", "undefined"},
      /* 85 */ {"var85", "undefined"},
      /* 86 */ {"var86", "undefined"},
      /* 87 */ {"var87", "undefined"},
      /* 88 */ {"var88", "undefined"},
      /* 89 */ {"var89", "undefined"},
      /* 90 */ {"var90", "undefined"},
      /* 91 */ {"var91", "undefined"},
      /* 92 */ {"var92", "undefined"},
      /* 93 */ {"var93", "undefined"},
      /* 94 */ {"var94", "undefined"},
      /* 95 */ {"var95", "undefined"},
      /* 96 */ {"var96", "undefined"},
      /* 97 */ {"var97", "undefined"},
      /* 98 */ {"var98", "undefined"},
      /* 99 */ {"var99", "undefined"},
      /* 100 */ {"var100", "undefined"},
      /* 101 */ {"var101", "undefined"},
      /* 102 */ {"var102", "undefined"},
      /* 103 */ {"var103", "undefined"},
      /* 104 */ {"var104", "undefined"},
      /* 105 */ {"var105", "undefined"},
      /* 106 */ {"var106", "undefined"},
      /* 107 */ {"var107", "undefined"},
      /* 108 */ {"var108", "undefined"},
      /* 109 */ {"var109", "undefined"},
      /* 110 */ {"var110", "undefined"},
      /* 111 */ {"var111", "undefined"},
      /* 112 */ {"var112", "undefined"},
      /* 113 */ {"var113", "undefined"},
      /* 114 */ {"var114", "undefined"},
      /* 115 */ {"var115", "undefined"},
      /* 116 */ {"var116", "undefined"},
      /* 117 */ {"var117", "undefined"},
      /* 118 */ {"var118", "undefined"},
      /* 119 */ {"var119", "undefined"},
      /* 120 */ {"var120", "undefined"},
      /* 121 */ {"var121", "undefined"},
      /* 122 */ {"var122", "undefined"},
      /* 123 */ {"var123", "undefined"},
      /* 124 */ {"var124", "undefined"},
      /* 125 */ {"var125", "undefined"},
      /* 126 */ {"var126", "undefined"},
      /* 127 */ {"NONE", "RESERVED"},
      /* 128 */ {"NONE", "RESERVED"},
      /* 129 */ {"PT", "Potential temperature deg C"},
      /* 130 */ {"S", "Salinity"},
      /* 131 */ {"U", "U-velocity m s**-1"},
      /* 132 */ {"V", "V-velocity m s**-1"},
      /* 133 */ {"WV", "W-velocity m s**-1"},
      /* 134 */ {"MST", "Modulus of strain rate tensor s**-1"},
      /* 135 */ {"VVS", "Vertical viscosity m**2 s**-1"},
      /* 136 */ {"VDF", "Vertical diffusivity m**2 s**-1"},
      /* 137 */ {"DEP", "Depth m"},
      /* 138 */ {"STH", "Sigma-theta kg m**-3"},
      /* 139 */ {"RN", "Richardson number -"},
      /* 140 */ {"var140", "undefined"},
      /* 141 */ {"var141", "undefined"},
      /* 142 */ {"var142", "undefined"},
      /* 143 */ {"var143", "undefined"},
      /* 144 */ {"var144", "undefined"},
      /* 145 */ {"SL", "Sea level (tides removed) m"},
      /* 146 */ {"SFT", "Sea floor topography m"},
      /* 147 */ {"BSF", "Barotropic streamfunction m**3 s**-1"},
      /* 148 */ {"MLD", "Mixed layer depth m"},
      /* 149 */ {"var149", "undefined"},
      /* 150 */ {"var150", "undefined"},
      /* 151 */ {"var151", "undefined"},
      /* 152 */ {"NONE", "RESERVED"},
      /* 153 */ {"TAX", "U-stress Pa"},
      /* 154 */ {"TAY", "V-stress Pa"},
      /* 155 */ {"TKI", "Turbulent kinetic energy input W m**-2"},
      /* 156 */ {"NSF", "Net surface heat flux W m**-2"},
      /* 157 */ {"ASR", "Absorbed solar radiation W m**-2"},
      /* 158 */ {"PME", "Precipitation - evaporation m s**-1"},
      /* 159 */ {"SST", "Specified SST deg C"},
      /* 160 */ {"SHF", "Specified surface heat flux W m**-2"},
      /* 161 */ {"DTE", "Diagnosed SST error deg C"},
      /* 162 */ {"HFC", "Heat flux correction W m**-2"},
      /* 163 */ {"var163", "undefined"},
      /* 164 */ {"var164", "undefined"},
      /* 165 */ {"var165", "undefined"},
      /* 166 */ {"var166", "undefined"},
      /* 167 */ {"var167", "undefined"},
      /* 168 */ {"var168", "undefined"},
      /* 169 */ {"var169", "undefined"},
      /* 170 */ {"var170", "undefined"},
      /* 171 */ {"var171", "undefined"},
      /* 172 */ {"var172", "undefined"},
      /* 173 */ {"var173", "undefined"},
      /* 174 */ {"var174", "undefined"},
      /* 175 */ {"var175", "undefined"},
      /* 176 */ {"var176", "undefined"},
      /* 177 */ {"var177", "undefined"},
      /* 178 */ {"var178", "undefined"},
      /* 179 */ {"var179", "undefined"},
      /* 180 */ {"var180", "undefined"},
      /* 181 */ {"var181", "undefined"},
      /* 182 */ {"var182", "undefined"},
      /* 183 */ {"var183", "undefined"},
      /* 184 */ {"var184", "undefined"},
      /* 185 */ {"var185", "undefined"},
      /* 186 */ {"var186", "undefined"},
      /* 187 */ {"var187", "undefined"},
      /* 188 */ {"var188", "undefined"},
      /* 189 */ {"var189", "undefined"},
      /* 190 */ {"var190", "undefined"},
      /* 191 */ {"var191", "undefined"},
      /* 192 */ {"var192", "undefined"},
      /* 193 */ {"var193", "undefined"},
      /* 194 */ {"var194", "undefined"},
      /* 195 */ {"var195", "undefined"},
      /* 196 */ {"var196", "undefined"},
      /* 197 */ {"var197", "undefined"},
      /* 198 */ {"var198", "undefined"},
      /* 199 */ {"var199", "undefined"},
      /* 200 */ {"var200", "undefined"},
      /* 201 */ {"var201", "undefined"},
      /* 202 */ {"var202", "undefined"},
      /* 203 */ {"var203", "undefined"},
      /* 204 */ {"var204", "undefined"},
      /* 205 */ {"var205", "undefined"},
      /* 206 */ {"var206", "undefined"},
      /* 207 */ {"var207", "undefined"},
      /* 208 */ {"var208", "undefined"},
      /* 209 */ {"var209", "undefined"},
      /* 210 */ {"var210", "undefined"},
      /* 211 */ {"var211", "undefined"},
      /* 212 */ {"var212", "undefined"},
      /* 213 */ {"var213", "undefined"},
      /* 214 */ {"var214", "undefined"},
      /* 215 */ {"var215", "undefined"},
      /* 216 */ {"var216", "undefined"},
      /* 217 */ {"var217", "undefined"},
      /* 218 */ {"var218", "undefined"},
      /* 219 */ {"var219", "undefined"},
      /* 220 */ {"var220", "undefined"},
      /* 221 */ {"var221", "undefined"},
      /* 222 */ {"var222", "undefined"},
      /* 223 */ {"var223", "undefined"},
      /* 224 */ {"var224", "undefined"},
      /* 225 */ {"var225", "undefined"},
      /* 226 */ {"var226", "undefined"},
      /* 227 */ {"var227", "undefined"},
      /* 228 */ {"var228", "undefined"},
      /* 229 */ {"var229", "undefined"},
      /* 230 */ {"var230", "undefined"},
      /* 231 */ {"var231", "undefined"},
      /* 232 */ {"var232", "undefined"},
      /* 233 */ {"var233", "undefined"},
      /* 234 */ {"var234", "undefined"},
      /* 235 */ {"var235", "undefined"},
      /* 236 */ {"var236", "undefined"},
      /* 237 */ {"var237", "undefined"},
      /* 238 */ {"var238", "undefined"},
      /* 239 */ {"var239", "undefined"},
      /* 240 */ {"var240", "undefined"},
      /* 241 */ {"var241", "undefined"},
      /* 242 */ {"var242", "undefined"},
      /* 243 */ {"var243", "undefined"},
      /* 244 */ {"var244", "undefined"},
      /* 245 */ {"var245", "undefined"},
      /* 246 */ {"var246", "undefined"},
      /* 247 */ {"var247", "undefined"},
      /* 248 */ {"var248", "undefined"},
      /* 249 */ {"var249", "undefined"},
      /* 250 */ {"var250", "undefined"},
      /* 251 */ {"var251", "undefined"},
      /* 252 */ {"var252", "undefined"},
      /* 253 */ {"var253", "undefined"},
      /* 254 */ {"var254", "undefined"},
      /* 255 */ {"NONE", "RESERVED"},
};


struct ParmTable parm_table_ecmwf_160[256] = {
    /* 0 */ {"var0", "undefined"},
    /* 1 */ {"var1", "undefined"},
    /* 2 */ {"var2", "undefined"},
    /* 3 */ {"var3", "undefined"},
    /* 4 */ {"var4", "undefined"},
    /* 5 */ {"var5", "undefined"},
    /* 6 */ {"var6", "undefined"},
    /* 7 */ {"var7", "undefined"},
    /* 8 */ {"var8", "undefined"},
    /* 9 */ {"var9", "undefined"},
    /* 10 */ {"var10", "undefined"},
    /* 11 */ {"var11", "undefined"},
    /* 12 */ {"var12", "undefined"},
    /* 13 */ {"var13", "undefined"},
    /* 14 */ {"var14", "undefined"},
    /* 15 */ {"var15", "undefined"},
    /* 16 */ {"var16", "undefined"},
    /* 17 */ {"var17", "undefined"},
    /* 18 */ {"var18", "undefined"},
    /* 19 */ {"var19", "undefined"},
    /* 20 */ {"var20", "undefined"},
    /* 21 */ {"var21", "undefined"},
    /* 22 */ {"var22", "undefined"},
    /* 23 */ {"var23", "undefined"},
    /* 24 */ {"var24", "undefined"},
    /* 25 */ {"var25", "undefined"},
    /* 26 */ {"var26", "undefined"},
    /* 27 */ {"var27", "undefined"},
    /* 28 */ {"var28", "undefined"},
    /* 29 */ {"var29", "undefined"},
    /* 30 */ {"var30", "undefined"},
    /* 31 */ {"var31", "undefined"},
    /* 32 */ {"var32", "undefined"},
    /* 33 */ {"var33", "undefined"},
    /* 34 */ {"var34", "undefined"},
    /* 35 */ {"var35", "undefined"},
    /* 36 */ {"var36", "undefined"},
    /* 37 */ {"var37", "undefined"},
    /* 38 */ {"var38", "undefined"},
    /* 39 */ {"var39", "undefined"},
    /* 40 */ {"var40", "undefined"},
    /* 41 */ {"var41", "undefined"},
    /* 42 */ {"var42", "undefined"},
    /* 43 */ {"var43", "undefined"},
    /* 44 */ {"var44", "undefined"},
    /* 45 */ {"var45", "undefined"},
    /* 46 */ {"var46", "undefined"},
    /* 47 */ {"var47", "undefined"},
    /* 48 */ {"var48", "undefined"},
    /* 49 */ {"var49", "undefined"},
    /* 50 */ {"var50", "undefined"},
    /* 51 */ {"var51", "undefined"},
    /* 52 */ {"var52", "undefined"},
    /* 53 */ {"var53", "undefined"},
    /* 54 */ {"var54", "undefined"},
    /* 55 */ {"var55", "undefined"},
    /* 56 */ {"var56", "undefined"},
    /* 57 */ {"var57", "undefined"},
    /* 58 */ {"var58", "undefined"},
    /* 59 */ {"var59", "undefined"},
    /* 60 */ {"var60", "undefined"},
    /* 61 */ {"var61", "undefined"},
    /* 62 */ {"var62", "undefined"},
    /* 63 */ {"var63", "undefined"},
    /* 64 */ {"var64", "undefined"},
    /* 65 */ {"var65", "undefined"},
    /* 66 */ {"var66", "undefined"},
    /* 67 */ {"var67", "undefined"},
    /* 68 */ {"var68", "undefined"},
    /* 69 */ {"var69", "undefined"},
    /* 70 */ {"var70", "undefined"},
    /* 71 */ {"var71", "undefined"},
    /* 72 */ {"var72", "undefined"},
    /* 73 */ {"var73", "undefined"},
    /* 74 */ {"var74", "undefined"},
    /* 75 */ {"var75", "undefined"},
    /* 76 */ {"var76", "undefined"},
    /* 77 */ {"var77", "undefined"},
    /* 78 */ {"var78", "undefined"},
    /* 79 */ {"var79", "undefined"},
    /* 80 */ {"var80", "undefined"},
    /* 81 */ {"var81", "undefined"},
    /* 82 */ {"var82", "undefined"},
    /* 83 */ {"var83", "undefined"},
    /* 84 */ {"var84", "undefined"},
    /* 85 */ {"var85", "undefined"},
    /* 86 */ {"var86", "undefined"},
    /* 87 */ {"var87", "undefined"},
    /* 88 */ {"var88", "undefined"},
    /* 89 */ {"var89", "undefined"},
    /* 90 */ {"var90", "undefined"},
    /* 91 */ {"var91", "undefined"},
    /* 92 */ {"var92", "undefined"},
    /* 93 */ {"var93", "undefined"},
    /* 94 */ {"var94", "undefined"},
    /* 95 */ {"var95", "undefined"},
    /* 96 */ {"var96", "undefined"},
    /* 97 */ {"var97", "undefined"},
    /* 98 */ {"var98", "undefined"},
    /* 99 */ {"var99", "undefined"},
    /* 100 */ {"var100", "undefined"},
    /* 101 */ {"var101", "undefined"},
    /* 102 */ {"var102", "undefined"},
    /* 103 */ {"var103", "undefined"},
    /* 104 */ {"var104", "undefined"},
    /* 105 */ {"var105", "undefined"},
    /* 106 */ {"var106", "undefined"},
    /* 107 */ {"var107", "undefined"},
    /* 108 */ {"var108", "undefined"},
    /* 109 */ {"var109", "undefined"},
    /* 110 */ {"var110", "undefined"},
    /* 111 */ {"var111", "undefined"},
    /* 112 */ {"var112", "undefined"},
    /* 113 */ {"var113", "undefined"},
    /* 114 */ {"var114", "undefined"},
    /* 115 */ {"var115", "undefined"},
    /* 116 */ {"var116", "undefined"},
    /* 117 */ {"var117", "undefined"},
    /* 118 */ {"var118", "undefined"},
    /* 119 */ {"var119", "undefined"},
    /* 120 */ {"var120", "undefined"},
    /* 121 */ {"var121", "undefined"},
    /* 122 */ {"var122", "undefined"},
    /* 123 */ {"var123", "undefined"},
    /* 124 */ {"var124", "undefined"},
    /* 125 */ {"var125", "undefined"},
    /* 126 */ {"var126", "undefined"},
    /* 127 */ {"AT", "Atmospheric tide+ -"},
    /* 128 */ {"BV", "Budget values+ -"},
    /* 129 */ {"Z", "Geopotential / orography m**2 s**-2"},
    /* 130 */ {"T", "Temperature K"},
    /* 131 */ {"U", "U-velocity m s**-1"},
    /* 132 */ {"V", "V-velocity m s**-1"},
    /* 133 */ {"Q", "Specific humidity kg kg**-1"},
    /* 134 */ {"SP", "Surface pressure Pa"},
    /* 135 */ {"W", "Vertical velocity Pa s**-1"},
    /* 136 */ {"var136", "undefined"},
    /* 137 */ {"PWC", "Precipitable water content kg m**-2"},
    /* 138 */ {"VO", "Vorticity (relative) s**-1"},
    /* 139 */ {"STL1", "Soil temperature level 1 K"},
    /* 140 */ {"SWL1", "Soil wetness level 1 m"},
    /* 141 */ {"SD", "Snow depth m (of water)"},
    /* 142 */ {"LSP", "Large scale precipitation kg m**-2 s**-1"},
    /* 143 */ {"CP", "Convective precipitation kg m**-2 s**-1"},
    /* 144 */ {"SF", "Snow fall kg m**-2 s**-1"},
    /* 145 */ {"BLD", "Boundary layer dissipation W m**-2"},
    /* 146 */ {"SSHF", "Surface sensible heat flux W m**-2"},
    /* 147 */ {"SLHF", "Surface latent heat flux W m**-2"},
    /* 148 */ {"var148", "undefined"},
    /* 149 */ {"var149", "undefined"},
    /* 150 */ {"var150", "undefined"},
    /* 151 */ {"MSL", "Mean sea level pressure Pa"},
    /* 152 */ {"LNSP", "Ln surface pressure -"},
    /* 153 */ {"var153", "undefined"},
    /* 154 */ {"var154", "undefined"},
    /* 155 */ {"D", "Divergence s**-1"},
    /* 156 */ {"GH", "Height (geopotential) m"},
    /* 157 */ {"R", "Relative humidity (0 - 1)"},
    /* 158 */ {"TSP", "Tendency of surface pressure Pa s**-1"},
    /* 159 */ {"var159", "undefined"},
    /* 160 */ {"var160", "undefined"},
    /* 161 */ {"var161", "undefined"},
    /* 162 */ {"var162", "undefined"},
    /* 163 */ {"var163", "undefined"},
    /* 164 */ {"TCC", "Total cloud cover (0 - 1)"},
    /* 165 */ {"10U", "10 metre u wind component m s**-1"},
    /* 166 */ {"10V", "10 metre v wind component m s**-1"},
    /* 167 */ {"2T", "2 metre temperature K"},
    /* 168 */ {"2D", "2 metre dewpoint temperature K"},
    /* 169 */ {"var169", "undefined"},
    /* 170 */ {"STL2", "Soil temperature level 2 K"},
    /* 171 */ {"SWL2", "Soil wetness level 2 m"},
    /* 172 */ {"LSM", "Land/sea mask (0 - 1)"},
    /* 173 */ {"SR", "Surface roughness m"},
    /* 174 */ {"AL", "Albedo (0 - 1)"},
    /* 175 */ {"var175", "undefined"},
    /* 176 */ {"SSR", "Surface solar radiation W m**-2"},
    /* 177 */ {"STR", "Surface thermal radiation W m**-2"},
    /* 178 */ {"TSR", "Top solar radiation W m**-2"},
    /* 179 */ {"TTR", "Top thermal radiation W m**-2"},
    /* 180 */ {"EWSS", "East/west surface stress N m**-2 s**-1"},
    /* 181 */ {"NSSS", "North/south surface stress N m**-2 s**-1"},
    /* 182 */ {"E", "Evaporation kg m**-2 s**-1"},
    /* 183 */ {"STL3", "Soil temperature level 3 K"},
    /* 184 */ {"SWL3", "Soil wetness level 3 m"},
    /* 185 */ {"CCC", "Convective cloud cover (0 - 1)"},
    /* 186 */ {"LCC", "Low cloud cover (0 - 1)"},
    /* 187 */ {"MCC", "Medium cloud cover (0 - 1)"},
    /* 188 */ {"HCC", "High cloud cover (0 - 1)"},
    /* 189 */ {"var189", "undefined"},
    /* 190 */ {"EWOV", "EW component of sub-grid scale orographic variance m**2"},
    /* 191 */ {"NSOV", "NS component of sub-grid scale orographic variance m**2"},
    /* 192 */ {"NWOV", "NWSE component sub-grid scale orographic variance m**2"},
    /* 193 */ {"NEOV", "NESW component sub-grid scale orographic variance m**2"},
    /* 194 */ {"var194", "undefined"},
    /* 195 */ {"LGWS", "Latitudinal component of gravity wave stress N m**-2 s"},
    /* 196 */ {"MGWS", "Meridional component of gravity wave stress N m**-2 s"},
    /* 197 */ {"GWD", "Gravity wave dissipation W m**-2 s"},
    /* 198 */ {"SRC", "Skin reservoir content m (of water)"},
    /* 199 */ {"VEG", "Percentage of vegetation %"},
    /* 200 */ {"VSO", "Variance of sub-grid scale orography m**2"},
    /* 201 */ {"MX2T", "Max temp.2m during averaging time K"},
    /* 202 */ {"MN2T", "Min temp.2m during averaging time K"},
    /* 203 */ {"var203", "undefined"},
    /* 204 */ {"PAW", "Precip. analysis weights -"},
    /* 205 */ {"RO", "Runoff kg m**-2 s**-1"},
    /* 206 */ {"ZZ", "St.Dev. of Geopotential m**2 s**-2"},
    /* 207 */ {"TZ", "Covar Temp & Geopotential K m**2 s**-2"},
    /* 208 */ {"TT", "St.Dev. of Temperature K"},
    /* 209 */ {"QZ", "Covar Sp.Hum. & Geopotential m**2 s**-2"},
    /* 210 */ {"QT", "Covar Sp.Hum & Temp. K"},
    /* 211 */ {"QQ", "St.Dev. of Specific humidity (0 - 1)"},
    /* 212 */ {"UZ", "Covar U-comp. & Geopotential m**3 s**-3"},
    /* 213 */ {"UT", "Covar U-comp. & Temp. K m s**-1"},
    /* 214 */ {"UQ", "Covar U-comp. & Sp.Hum. m s**-1"},
    /* 215 */ {"UU", "St.Dev. of U-velocity m s**-1"},
    /* 216 */ {"VZ", "Covar V-comp. & Geopotential m**3 s**-3"},
    /* 217 */ {"VT", "Covar V-comp. & Temp. K m s**-1"},
    /* 218 */ {"VQ", "Covar V-comp. & Sp.Hum. m s**-1"},
    /* 219 */ {"VU", "Covar V-comp. & U-comp m**2 s**-2"},
    /* 220 */ {"VV", "St.Dev. of V-comp m s**-1"},
    /* 221 */ {"WZ", "Covar W-comp. & Geopotential Pa m**2 s**-3"},
    /* 222 */ {"WT", "Covar W-comp. & Temp. K Pa s**-1"},
    /* 223 */ {"WQ", "Covar W-comp. & Sp.Hum. Pa s**-1"},
    /* 224 */ {"WU", "Covar W-comp. & U-comp. Pa m s**-2"},
    /* 225 */ {"WV", "Covar W-comp. & V-comp. Pa m s**-2"},
    /* 226 */ {"WW", "St.Dev. of Vertical velocity Pa s**-1"},
    /* 227 */ {"var227", "undefined"},
    /* 228 */ {"TP", "Total precipitation m"},
    /* 229 */ {"IEWS", "Instantaneous X surface stress N m**-2"},
    /* 230 */ {"INSS", "Instantaneous Y surface stress N m**-2"},
    /* 231 */ {"ISHF", "Instantaneous surface Heat Flux W m**-2"},
    /* 232 */ {"IE", "Instantaneous Moisture Flux (evaporation) kg m**-2 s**-1"},
    /* 233 */ {"ASQ", "Apparent Surface Humidity kg kg**-1"},
    /* 234 */ {"LSRH", "Logarithm of surface roughness length for heat. -"},
    /* 235 */ {"SKT", "Skin Temperature K"},
    /* 236 */ {"STL4", "Soil temperature level 4 K"},
    /* 237 */ {"SWL4", "Soil wetness level 4 m"},
    /* 238 */ {"TSN", "Temperature of snow layer K"},
    /* 239 */ {"CSF", "Convective snow-fall kg m**-2 s**-1"},
    /* 240 */ {"LSF", "Large scale snow-fall kg m**-2 s**-1"},
    /* 241 */ {"CLWC", "Cloud liquid water content kg kg**-1"},
    /* 242 */ {"CC", "Cloud cover (at given level) (0 - 1)"},
    /* 243 */ {"FAL", "Forecast albedo -"},
    /* 244 */ {"FSR", "Forecast surface roughness m"},
    /* 245 */ {"FLSR", "Forecast logarithm of surface roughness for heat. -"},
    /* 246 */ {"10WS", "10m. Windspeed (irresp of dir.) m s**-1"},
    /* 247 */ {"MOFL", "Momentum flux (irresp of dir.) N m**-2"},
    /* 248 */ {"HSD", "Heaviside (beta) function (0 - 1)"},
    /* 249 */ {"var249", "undefined"},
    /* 250 */ {"var250", "undefined"},
    /* 251 */ {"var251", "undefined"},
    /* 252 */ {"var252", "undefined"},
    /* 253 */ {"var253", "undefined"},
    /* 254 */ {"var254", "undefined"},
    /* 255 */ {"var255", "undefined"},
};


struct ParmTable parm_table_ecmwf_170[256] = {
    /* 0 */ {"var0", "undefined"},
    /* 1 */ {"var1", "undefined"},
    /* 2 */ {"var2", "undefined"},
    /* 3 */ {"var3", "undefined"},
    /* 4 */ {"var4", "undefined"},
    /* 5 */ {"var5", "undefined"},
    /* 6 */ {"var6", "undefined"},
    /* 7 */ {"var7", "undefined"},
    /* 8 */ {"var8", "undefined"},
    /* 9 */ {"var9", "undefined"},
    /* 10 */ {"var10", "undefined"},
    /* 11 */ {"var11", "undefined"},
    /* 12 */ {"var12", "undefined"},
    /* 13 */ {"var13", "undefined"},
    /* 14 */ {"var14", "undefined"},
    /* 15 */ {"var15", "undefined"},
    /* 16 */ {"var16", "undefined"},
    /* 17 */ {"var17", "undefined"},
    /* 18 */ {"var18", "undefined"},
    /* 19 */ {"var19", "undefined"},
    /* 20 */ {"var20", "undefined"},
    /* 21 */ {"var21", "undefined"},
    /* 22 */ {"var22", "undefined"},
    /* 23 */ {"var23", "undefined"},
    /* 24 */ {"var24", "undefined"},
    /* 25 */ {"var25", "undefined"},
    /* 26 */ {"var26", "undefined"},
    /* 27 */ {"var27", "undefined"},
    /* 28 */ {"var28", "undefined"},
    /* 29 */ {"var29", "undefined"},
    /* 30 */ {"var30", "undefined"},
    /* 31 */ {"var31", "undefined"},
    /* 32 */ {"var32", "undefined"},
    /* 33 */ {"var33", "undefined"},
    /* 34 */ {"var34", "undefined"},
    /* 35 */ {"var35", "undefined"},
    /* 36 */ {"var36", "undefined"},
    /* 37 */ {"var37", "undefined"},
    /* 38 */ {"var38", "undefined"},
    /* 39 */ {"var39", "undefined"},
    /* 40 */ {"var40", "undefined"},
    /* 41 */ {"var41", "undefined"},
    /* 42 */ {"var42", "undefined"},
    /* 43 */ {"var43", "undefined"},
    /* 44 */ {"var44", "undefined"},
    /* 45 */ {"var45", "undefined"},
    /* 46 */ {"var46", "undefined"},
    /* 47 */ {"var47", "undefined"},
    /* 48 */ {"var48", "undefined"},
    /* 49 */ {"var49", "undefined"},
    /* 50 */ {"var50", "undefined"},
    /* 51 */ {"var51", "undefined"},
    /* 52 */ {"var52", "undefined"},
    /* 53 */ {"var53", "undefined"},
    /* 54 */ {"var54", "undefined"},
    /* 55 */ {"var55", "undefined"},
    /* 56 */ {"var56", "undefined"},
    /* 57 */ {"var57", "undefined"},
    /* 58 */ {"var58", "undefined"},
    /* 59 */ {"var59", "undefined"},
    /* 60 */ {"var60", "undefined"},
    /* 61 */ {"var61", "undefined"},
    /* 62 */ {"var62", "undefined"},
    /* 63 */ {"var63", "undefined"},
    /* 64 */ {"var64", "undefined"},
    /* 65 */ {"var65", "undefined"},
    /* 66 */ {"var66", "undefined"},
    /* 67 */ {"var67", "undefined"},
    /* 68 */ {"var68", "undefined"},
    /* 69 */ {"var69", "undefined"},
    /* 70 */ {"var70", "undefined"},
    /* 71 */ {"var71", "undefined"},
    /* 72 */ {"var72", "undefined"},
    /* 73 */ {"var73", "undefined"},
    /* 74 */ {"var74", "undefined"},
    /* 75 */ {"var75", "undefined"},
    /* 76 */ {"var76", "undefined"},
    /* 77 */ {"var77", "undefined"},
    /* 78 */ {"var78", "undefined"},
    /* 79 */ {"var79", "undefined"},
    /* 80 */ {"var80", "undefined"},
    /* 81 */ {"var81", "undefined"},
    /* 82 */ {"var82", "undefined"},
    /* 83 */ {"var83", "undefined"},
    /* 84 */ {"var84", "undefined"},
    /* 85 */ {"var85", "undefined"},
    /* 86 */ {"var86", "undefined"},
    /* 87 */ {"var87", "undefined"},
    /* 88 */ {"var88", "undefined"},
    /* 89 */ {"var89", "undefined"},
    /* 90 */ {"var90", "undefined"},
    /* 91 */ {"var91", "undefined"},
    /* 92 */ {"var92", "undefined"},
    /* 93 */ {"var93", "undefined"},
    /* 94 */ {"var94", "undefined"},
    /* 95 */ {"var95", "undefined"},
    /* 96 */ {"var96", "undefined"},
    /* 97 */ {"var97", "undefined"},
    /* 98 */ {"var98", "undefined"},
    /* 99 */ {"var99", "undefined"},
    /* 100 */ {"var100", "undefined"},
    /* 101 */ {"var101", "undefined"},
    /* 102 */ {"var102", "undefined"},
    /* 103 */ {"var103", "undefined"},
    /* 104 */ {"var104", "undefined"},
    /* 105 */ {"var105", "undefined"},
    /* 106 */ {"var106", "undefined"},
    /* 107 */ {"var107", "undefined"},
    /* 108 */ {"var108", "undefined"},
    /* 109 */ {"var109", "undefined"},
    /* 110 */ {"var110", "undefined"},
    /* 111 */ {"var111", "undefined"},
    /* 112 */ {"var112", "undefined"},
    /* 113 */ {"var113", "undefined"},
    /* 114 */ {"var114", "undefined"},
    /* 115 */ {"var115", "undefined"},
    /* 116 */ {"var116", "undefined"},
    /* 117 */ {"var117", "undefined"},
    /* 118 */ {"var118", "undefined"},
    /* 119 */ {"var119", "undefined"},
    /* 120 */ {"var120", "undefined"},
    /* 121 */ {"var121", "undefined"},
    /* 122 */ {"var122", "undefined"},
    /* 123 */ {"var123", "undefined"},
    /* 124 */ {"var124", "undefined"},
    /* 125 */ {"var125", "undefined"},
    /* 126 */ {"var126", "undefined"},
    /* 127 */ {"var127", "undefined"},
    /* 128 */ {"var128", "undefined"},
    /* 129 */ {"Z", "Geopotential m**2 s**-2"},
    /* 130 */ {"T", "Temperature K"},
    /* 131 */ {"U", "U-velocity m s**-1"},
    /* 132 */ {"V", "V-velocity m s**-1"},
    /* 133 */ {"var133", "undefined"},
    /* 134 */ {"var134", "undefined"},
    /* 135 */ {"var135", "undefined"},
    /* 136 */ {"var136", "undefined"},
    /* 137 */ {"var137", "undefined"},
    /* 138 */ {"VO", "Vorticity (relative) s**-1"},
    /* 139 */ {"var139", "undefined"},
    /* 140 */ {"SWL1", "Soil wetness level 1 m"},
    /* 141 */ {"SD", "Snow depth m (of water equivalent)"},
    /* 142 */ {"var142", "undefined"},
    /* 143 */ {"var143", "undefined"},
    /* 144 */ {"var144", "undefined"},
    /* 145 */ {"var145", "undefined"},
    /* 146 */ {"var146", "undefined"},
    /* 147 */ {"var147", "undefined"},
    /* 148 */ {"var148", "undefined"},
    /* 149 */ {"TSW", "Total soil moisture m"},
    /* 150 */ {"var150", "undefined"},
    /* 151 */ {"MSL", "Mean sea level pressure Pa"},
    /* 152 */ {"var152", "undefined"},
    /* 153 */ {"var153", "undefined"},
    /* 154 */ {"var154", "undefined"},
    /* 155 */ {"D", "Divergence s**-1"},
    /* 156 */ {"var156", "undefined"},
    /* 157 */ {"var157", "undefined"},
    /* 158 */ {"var158", "undefined"},
    /* 159 */ {"var159", "undefined"},
    /* 160 */ {"var160", "undefined"},
    /* 161 */ {"var161", "undefined"},
    /* 162 */ {"var162", "undefined"},
    /* 163 */ {"var163", "undefined"},
    /* 164 */ {"var164", "undefined"},
    /* 165 */ {"var165", "undefined"},
    /* 166 */ {"var166", "undefined"},
    /* 167 */ {"var167", "undefined"},
    /* 168 */ {"var168", "undefined"},
    /* 169 */ {"var169", "undefined"},
    /* 170 */ {"var170", "undefined"},
    /* 171 */ {"SWL2", "Soil wetness level 2 m"},
    /* 172 */ {"var172", "undefined"},
    /* 173 */ {"var173", "undefined"},
    /* 174 */ {"var174", "undefined"},
    /* 175 */ {"var175", "undefined"},
    /* 176 */ {"var176", "undefined"},
    /* 177 */ {"var177", "undefined"},
    /* 178 */ {"var178", "undefined"},
    /* 179 */ {"TTR", "Top thermal radiation W m-2"},
    /* 180 */ {"var180", "undefined"},
    /* 181 */ {"var181", "undefined"},
    /* 182 */ {"var182", "undefined"},
    /* 183 */ {"var183", "undefined"},
    /* 184 */ {"SWL3", "Soil wetness level 3 m"},
    /* 185 */ {"var185", "undefined"},
    /* 186 */ {"var186", "undefined"},
    /* 187 */ {"var187", "undefined"},
    /* 188 */ {"var188", "undefined"},
    /* 189 */ {"var189", "undefined"},
    /* 190 */ {"var190", "undefined"},
    /* 191 */ {"var191", "undefined"},
    /* 192 */ {"var192", "undefined"},
    /* 193 */ {"var193", "undefined"},
    /* 194 */ {"var194", "undefined"},
    /* 195 */ {"var195", "undefined"},
    /* 196 */ {"var196", "undefined"},
    /* 197 */ {"var197", "undefined"},
    /* 198 */ {"var198", "undefined"},
    /* 199 */ {"var199", "undefined"},
    /* 200 */ {"var200", "undefined"},
    /* 201 */ {"MX2T", "Max temp at 2m since previous postprocess K"},
    /* 202 */ {"MN2T", "Min temp at 2m since previous postprocess K"},
    /* 203 */ {"var203", "undefined"},
    /* 204 */ {"var204", "undefined"},
    /* 205 */ {"var205", "undefined"},
    /* 206 */ {"var206", "undefined"},
    /* 207 */ {"var207", "undefined"},
    /* 208 */ {"var208", "undefined"},
    /* 209 */ {"var209", "undefined"},
    /* 210 */ {"var210", "undefined"},
    /* 211 */ {"var211", "undefined"},
    /* 212 */ {"var212", "undefined"},
    /* 213 */ {"var213", "undefined"},
    /* 214 */ {"var214", "undefined"},
    /* 215 */ {"var215", "undefined"},
    /* 216 */ {"var216", "undefined"},
    /* 217 */ {"var217", "undefined"},
    /* 218 */ {"var218", "undefined"},
    /* 219 */ {"var219", "undefined"},
    /* 220 */ {"var220", "undefined"},
    /* 221 */ {"var221", "undefined"},
    /* 222 */ {"var222", "undefined"},
    /* 223 */ {"var223", "undefined"},
    /* 224 */ {"var224", "undefined"},
    /* 225 */ {"var225", "undefined"},
    /* 226 */ {"var226", "undefined"},
    /* 227 */ {"var227", "undefined"},
    /* 228 */ {"TP", "Total precipitation m"},
    /* 229 */ {"var229", "undefined"},
    /* 230 */ {"var230", "undefined"},
    /* 231 */ {"var231", "undefined"},
    /* 232 */ {"var232", "undefined"},
    /* 233 */ {"var233", "undefined"},
    /* 234 */ {"var234", "undefined"},
    /* 235 */ {"var235", "undefined"},
    /* 236 */ {"var236", "undefined"},
    /* 237 */ {"var237", "undefined"},
    /* 238 */ {"var238", "undefined"},
    /* 239 */ {"var239", "undefined"},
    /* 240 */ {"var240", "undefined"},
    /* 241 */ {"var241", "undefined"},
    /* 242 */ {"var242", "undefined"},
    /* 243 */ {"var243", "undefined"},
    /* 244 */ {"var244", "undefined"},
    /* 245 */ {"var245", "undefined"},
    /* 246 */ {"var246", "undefined"},
    /* 247 */ {"var247", "undefined"},
    /* 248 */ {"var248", "undefined"},
    /* 249 */ {"var249", "undefined"},
    /* 250 */ {"var250", "undefined"},
    /* 251 */ {"var251", "undefined"},
    /* 252 */ {"var252", "undefined"},
    /* 253 */ {"var253", "undefined"},
    /* 254 */ {"var254", "undefined"},
    /* 255 */ {"var255", "undefined"},
};


struct ParmTable parm_table_ecmwf_180[256] = {
    /* 0 */ {"var0", "undefined"},
    /* 1 */ {"var1", "undefined"},
    /* 2 */ {"var2", "undefined"},
    /* 3 */ {"var3", "undefined"},
    /* 4 */ {"var4", "undefined"},
    /* 5 */ {"var5", "undefined"},
    /* 6 */ {"var6", "undefined"},
    /* 7 */ {"var7", "undefined"},
    /* 8 */ {"var8", "undefined"},
    /* 9 */ {"var9", "undefined"},
    /* 10 */ {"var10", "undefined"},
    /* 11 */ {"var11", "undefined"},
    /* 12 */ {"var12", "undefined"},
    /* 13 */ {"var13", "undefined"},
    /* 14 */ {"var14", "undefined"},
    /* 15 */ {"var15", "undefined"},
    /* 16 */ {"var16", "undefined"},
    /* 17 */ {"var17", "undefined"},
    /* 18 */ {"var18", "undefined"},
    /* 19 */ {"var19", "undefined"},
    /* 20 */ {"var20", "undefined"},
    /* 21 */ {"var21", "undefined"},
    /* 22 */ {"var22", "undefined"},
    /* 23 */ {"var23", "undefined"},
    /* 24 */ {"var24", "undefined"},
    /* 25 */ {"var25", "undefined"},
    /* 26 */ {"var26", "undefined"},
    /* 27 */ {"var27", "undefined"},
    /* 28 */ {"var28", "undefined"},
    /* 29 */ {"var29", "undefined"},
    /* 30 */ {"var30", "undefined"},
    /* 31 */ {"var31", "undefined"},
    /* 32 */ {"var32", "undefined"},
    /* 33 */ {"var33", "undefined"},
    /* 34 */ {"var34", "undefined"},
    /* 35 */ {"var35", "undefined"},
    /* 36 */ {"var36", "undefined"},
    /* 37 */ {"var37", "undefined"},
    /* 38 */ {"var38", "undefined"},
    /* 39 */ {"var39", "undefined"},
    /* 40 */ {"var40", "undefined"},
    /* 41 */ {"var41", "undefined"},
    /* 42 */ {"var42", "undefined"},
    /* 43 */ {"var43", "undefined"},
    /* 44 */ {"var44", "undefined"},
    /* 45 */ {"var45", "undefined"},
    /* 46 */ {"var46", "undefined"},
    /* 47 */ {"var47", "undefined"},
    /* 48 */ {"var48", "undefined"},
    /* 49 */ {"var49", "undefined"},
    /* 50 */ {"var50", "undefined"},
    /* 51 */ {"var51", "undefined"},
    /* 52 */ {"var52", "undefined"},
    /* 53 */ {"var53", "undefined"},
    /* 54 */ {"var54", "undefined"},
    /* 55 */ {"var55", "undefined"},
    /* 56 */ {"var56", "undefined"},
    /* 57 */ {"var57", "undefined"},
    /* 58 */ {"var58", "undefined"},
    /* 59 */ {"var59", "undefined"},
    /* 60 */ {"var60", "undefined"},
    /* 61 */ {"var61", "undefined"},
    /* 62 */ {"var62", "undefined"},
    /* 63 */ {"var63", "undefined"},
    /* 64 */ {"var64", "undefined"},
    /* 65 */ {"var65", "undefined"},
    /* 66 */ {"var66", "undefined"},
    /* 67 */ {"var67", "undefined"},
    /* 68 */ {"var68", "undefined"},
    /* 69 */ {"var69", "undefined"},
    /* 70 */ {"var70", "undefined"},
    /* 71 */ {"var71", "undefined"},
    /* 72 */ {"var72", "undefined"},
    /* 73 */ {"var73", "undefined"},
    /* 74 */ {"var74", "undefined"},
    /* 75 */ {"var75", "undefined"},
    /* 76 */ {"var76", "undefined"},
    /* 77 */ {"var77", "undefined"},
    /* 78 */ {"var78", "undefined"},
    /* 79 */ {"var79", "undefined"},
    /* 80 */ {"var80", "undefined"},
    /* 81 */ {"var81", "undefined"},
    /* 82 */ {"var82", "undefined"},
    /* 83 */ {"var83", "undefined"},
    /* 84 */ {"var84", "undefined"},
    /* 85 */ {"var85", "undefined"},
    /* 86 */ {"var86", "undefined"},
    /* 87 */ {"var87", "undefined"},
    /* 88 */ {"var88", "undefined"},
    /* 89 */ {"var89", "undefined"},
    /* 90 */ {"var90", "undefined"},
    /* 91 */ {"var91", "undefined"},
    /* 92 */ {"var92", "undefined"},
    /* 93 */ {"var93", "undefined"},
    /* 94 */ {"var94", "undefined"},
    /* 95 */ {"var95", "undefined"},
    /* 96 */ {"var96", "undefined"},
    /* 97 */ {"var97", "undefined"},
    /* 98 */ {"var98", "undefined"},
    /* 99 */ {"var99", "undefined"},
    /* 100 */ {"var100", "undefined"},
    /* 101 */ {"var101", "undefined"},
    /* 102 */ {"var102", "undefined"},
    /* 103 */ {"var103", "undefined"},
    /* 104 */ {"var104", "undefined"},
    /* 105 */ {"var105", "undefined"},
    /* 106 */ {"var106", "undefined"},
    /* 107 */ {"var107", "undefined"},
    /* 108 */ {"var108", "undefined"},
    /* 109 */ {"var109", "undefined"},
    /* 110 */ {"var110", "undefined"},
    /* 111 */ {"var111", "undefined"},
    /* 112 */ {"var112", "undefined"},
    /* 113 */ {"var113", "undefined"},
    /* 114 */ {"var114", "undefined"},
    /* 115 */ {"var115", "undefined"},
    /* 116 */ {"var116", "undefined"},
    /* 117 */ {"var117", "undefined"},
    /* 118 */ {"var118", "undefined"},
    /* 119 */ {"var119", "undefined"},
    /* 120 */ {"var120", "undefined"},
    /* 121 */ {"var121", "undefined"},
    /* 122 */ {"var122", "undefined"},
    /* 123 */ {"var123", "undefined"},
    /* 124 */ {"var124", "undefined"},
    /* 125 */ {"var125", "undefined"},
    /* 126 */ {"var126", "undefined"},
    /* 127 */ {"var127", "undefined"},
    /* 128 */ {"var128", "undefined"},
    /* 129 */ {"Z", "Geopotential (at the surface=orography) m**2 s**-2"},
    /* 130 */ {"T", "Temperature K"},
    /* 131 */ {"U", "U-velocity m s**-1"},
    /* 132 */ {"V", "V-velocity m s**-1"},
    /* 133 */ {"Q", "Specific humidity kg kg**-1"},
    /* 134 */ {"SP", "Surface pressure Pa"},
    /* 135 */ {"var135", "undefined"},
    /* 136 */ {"var136", "undefined"},
    /* 137 */ {"TCWV", "Total column water vapour kg m**-2"},
    /* 138 */ {"VO", "Vorticity (relative) s**-1"},
    /* 139 */ {"var139", "undefined"},
    /* 140 */ {"var140", "undefined"},
    /* 141 */ {"SD", "Snow depth m (of water equivalent)"},
    /* 142 */ {"LSP", "Large scale precipitation* m"},
    /* 143 */ {"CP", "Convective precipitation* m"},
    /* 144 */ {"SF", "Snow fall m(of water equivalent)"},
    /* 145 */ {"var145", "undefined"},
    /* 146 */ {"SSHF", "Surface sensible heat flux W m**-2 s"},
    /* 147 */ {"SLHF", "Surface latent heat flux W m**-2 s"},
    /* 148 */ {"var148", "undefined"},
    /* 149 */ {"TSW", "Total soil wetness m"},
    /* 150 */ {"var150", "undefined"},
    /* 151 */ {"MSL", "Mean sea level pressure Pa"},
    /* 152 */ {"var152", "undefined"},
    /* 153 */ {"var153", "undefined"},
    /* 154 */ {"var154", "undefined"},
    /* 155 */ {"D", "Divergence s**-1"},
    /* 156 */ {"var156", "undefined"},
    /* 157 */ {"var157", "undefined"},
    /* 158 */ {"var158", "undefined"},
    /* 159 */ {"var159", "undefined"},
    /* 160 */ {"var160", "undefined"},
    /* 161 */ {"var161", "undefined"},
    /* 162 */ {"var162", "undefined"},
    /* 163 */ {"var163", "undefined"},
    /* 164 */ {"TCC", "Total cloud cover (0 - 1)"},
    /* 165 */ {"10U", "10 metre u wind component m s**-1"},
    /* 166 */ {"10V", "10 metre v wind component m s**-1"},
    /* 167 */ {"2T", "2 metre temperature K"},
    /* 168 */ {"2D", "2 metre dewpoint temperature K"},
    /* 169 */ {"var169", "undefined"},
    /* 170 */ {"var170", "undefined"},
    /* 171 */ {"var171", "undefined"},
    /* 172 */ {"LSM", "Land/sea mask (0"},
    /* 173 */ {"var173", "undefined"},
    /* 174 */ {"var174", "undefined"},
    /* 175 */ {"var175", "undefined"},
    /* 176 */ {"SSR", "Surface solar radiation (net) J m**-2 s"},
    /* 177 */ {"STR", "Surface thermal radiation (net) J m**-2 s"},
    /* 178 */ {"TSR", "Top solar radiation (net) J m**-2 s"},
    /* 179 */ {"TTR", "Top thermal radiation (net) J m**-2 s"},
    /* 180 */ {"EWSS", "East/West surface stress N m**-2 s"},
    /* 181 */ {"NSSS", "North/South surface stress N m**-2 s"},
    /* 182 */ {"E", "Evaporation (surface) m (of water)"},
    /* 183 */ {"var183", "undefined"},
    /* 184 */ {"var184", "undefined"},
    /* 185 */ {"var185", "undefined"},
    /* 186 */ {"var186", "undefined"},
    /* 187 */ {"var187", "undefined"},
    /* 188 */ {"var188", "undefined"},
    /* 189 */ {"var189", "undefined"},
    /* 190 */ {"var190", "undefined"},
    /* 191 */ {"var191", "undefined"},
    /* 192 */ {"var192", "undefined"},
    /* 193 */ {"var193", "undefined"},
    /* 194 */ {"var194", "undefined"},
    /* 195 */ {"var195", "undefined"},
    /* 196 */ {"var196", "undefined"},
    /* 197 */ {"var197", "undefined"},
    /* 198 */ {"var198", "undefined"},
    /* 199 */ {"var199", "undefined"},
    /* 200 */ {"var200", "undefined"},
    /* 201 */ {"var201", "undefined"},
    /* 202 */ {"var202", "undefined"},
    /* 203 */ {"var203", "undefined"},
    /* 204 */ {"var204", "undefined"},
    /* 205 */ {"RO", "Runoff (total) m"},
    /* 206 */ {"var206", "undefined"},
    /* 207 */ {"var207", "undefined"},
    /* 208 */ {"var208", "undefined"},
    /* 209 */ {"var209", "undefined"},
    /* 210 */ {"var210", "undefined"},
    /* 211 */ {"var211", "undefined"},
    /* 212 */ {"var212", "undefined"},
    /* 213 */ {"var213", "undefined"},
    /* 214 */ {"var214", "undefined"},
    /* 215 */ {"var215", "undefined"},
    /* 216 */ {"var216", "undefined"},
    /* 217 */ {"var217", "undefined"},
    /* 218 */ {"var218", "undefined"},
    /* 219 */ {"var219", "undefined"},
    /* 220 */ {"var220", "undefined"},
    /* 221 */ {"var221", "undefined"},
    /* 222 */ {"var222", "undefined"},
    /* 223 */ {"var223", "undefined"},
    /* 224 */ {"var224", "undefined"},
    /* 225 */ {"var225", "undefined"},
    /* 226 */ {"var226", "undefined"},
    /* 227 */ {"var227", "undefined"},
    /* 228 */ {"var228", "undefined"},
    /* 229 */ {"var229", "undefined"},
    /* 230 */ {"var230", "undefined"},
    /* 231 */ {"var231", "undefined"},
    /* 232 */ {"var232", "undefined"},
    /* 233 */ {"var233", "undefined"},
    /* 234 */ {"var234", "undefined"},
    /* 235 */ {"var235", "undefined"},
    /* 236 */ {"var236", "undefined"},
    /* 237 */ {"var237", "undefined"},
    /* 238 */ {"var238", "undefined"},
    /* 239 */ {"var239", "undefined"},
    /* 240 */ {"var240", "undefined"},
    /* 241 */ {"var241", "undefined"},
    /* 242 */ {"var242", "undefined"},
    /* 243 */ {"var243", "undefined"},
    /* 244 */ {"var244", "undefined"},
    /* 245 */ {"var245", "undefined"},
    /* 246 */ {"var246", "undefined"},
    /* 247 */ {"var247", "undefined"},
    /* 248 */ {"var248", "undefined"},
    /* 249 */ {"var249", "undefined"},
    /* 250 */ {"var250", "undefined"},
    /* 251 */ {"var251", "undefined"},
    /* 252 */ {"var252", "undefined"},
    /* 253 */ {"var253", "undefined"},
    /* 254 */ {"var254", "undefined"},
    /* 255 */ {"var255", "undefined"},
};

struct ParmTable parm_table_ecmwf_190[256] = {
      /* 0 */ {"var0", "undefined"},
      /* 1 */ {"var1", "undefined"},
      /* 2 */ {"var2", "undefined"},
      /* 3 */ {"var3", "undefined"},
      /* 4 */ {"var4", "undefined"},
      /* 5 */ {"var5", "undefined"},
      /* 6 */ {"var6", "undefined"},
      /* 7 */ {"var7", "undefined"},
      /* 8 */ {"var8", "undefined"},
      /* 9 */ {"var9", "undefined"},
      /* 10 */ {"var10", "undefined"},
      /* 11 */ {"var11", "undefined"},
      /* 12 */ {"var12", "undefined"},
      /* 13 */ {"var13", "undefined"},
      /* 14 */ {"var14", "undefined"},
      /* 15 */ {"var15", "undefined"},
      /* 16 */ {"var16", "undefined"},
      /* 17 */ {"var17", "undefined"},
      /* 18 */ {"var18", "undefined"},
      /* 19 */ {"var19", "undefined"},
      /* 20 */ {"var20", "undefined"},
      /* 21 */ {"var21", "undefined"},
      /* 22 */ {"var22", "undefined"},
      /* 23 */ {"var23", "undefined"},
      /* 24 */ {"var24", "undefined"},
      /* 25 */ {"var25", "undefined"},
      /* 26 */ {"var26", "undefined"},
      /* 27 */ {"var27", "undefined"},
      /* 28 */ {"var28", "undefined"},
      /* 29 */ {"var29", "undefined"},
      /* 30 */ {"var30", "undefined"},
      /* 31 */ {"var31", "undefined"},
      /* 32 */ {"var32", "undefined"},
      /* 33 */ {"var33", "undefined"},
      /* 34 */ {"var34", "undefined"},
      /* 35 */ {"var35", "undefined"},
      /* 36 */ {"var36", "undefined"},
      /* 37 */ {"var37", "undefined"},
      /* 38 */ {"var38", "undefined"},
      /* 39 */ {"var39", "undefined"},
      /* 40 */ {"var40", "undefined"},
      /* 41 */ {"var41", "undefined"},
      /* 42 */ {"var42", "undefined"},
      /* 43 */ {"var43", "undefined"},
      /* 44 */ {"var44", "undefined"},
      /* 45 */ {"var45", "undefined"},
      /* 46 */ {"var46", "undefined"},
      /* 47 */ {"var47", "undefined"},
      /* 48 */ {"var48", "undefined"},
      /* 49 */ {"var49", "undefined"},
      /* 50 */ {"var50", "undefined"},
      /* 51 */ {"var51", "undefined"},
      /* 52 */ {"var52", "undefined"},
      /* 53 */ {"var53", "undefined"},
      /* 54 */ {"var54", "undefined"},
      /* 55 */ {"var55", "undefined"},
      /* 56 */ {"var56", "undefined"},
      /* 57 */ {"var57", "undefined"},
      /* 58 */ {"var58", "undefined"},
      /* 59 */ {"var59", "undefined"},
      /* 60 */ {"var60", "undefined"},
      /* 61 */ {"var61", "undefined"},
      /* 62 */ {"var62", "undefined"},
      /* 63 */ {"var63", "undefined"},
      /* 64 */ {"var64", "undefined"},
      /* 65 */ {"var65", "undefined"},
      /* 66 */ {"var66", "undefined"},
      /* 67 */ {"var67", "undefined"},
      /* 68 */ {"var68", "undefined"},
      /* 69 */ {"var69", "undefined"},
      /* 70 */ {"var70", "undefined"},
      /* 71 */ {"var71", "undefined"},
      /* 72 */ {"var72", "undefined"},
      /* 73 */ {"var73", "undefined"},
      /* 74 */ {"var74", "undefined"},
      /* 75 */ {"var75", "undefined"},
      /* 76 */ {"var76", "undefined"},
      /* 77 */ {"var77", "undefined"},
      /* 78 */ {"var78", "undefined"},
      /* 79 */ {"var79", "undefined"},
      /* 80 */ {"var80", "undefined"},
      /* 81 */ {"var81", "undefined"},
      /* 82 */ {"var82", "undefined"},
      /* 83 */ {"var83", "undefined"},
      /* 84 */ {"var84", "undefined"},
      /* 85 */ {"var85", "undefined"},
      /* 86 */ {"var86", "undefined"},
      /* 87 */ {"var87", "undefined"},
      /* 88 */ {"var88", "undefined"},
      /* 89 */ {"var89", "undefined"},
      /* 90 */ {"var90", "undefined"},
      /* 91 */ {"var91", "undefined"},
      /* 92 */ {"var92", "undefined"},
      /* 93 */ {"var93", "undefined"},
      /* 94 */ {"var94", "undefined"},
      /* 95 */ {"var95", "undefined"},
      /* 96 */ {"var96", "undefined"},
      /* 97 */ {"var97", "undefined"},
      /* 98 */ {"var98", "undefined"},
      /* 99 */ {"var99", "undefined"},
      /* 100 */ {"var100", "undefined"},
      /* 101 */ {"var101", "undefined"},
      /* 102 */ {"var102", "undefined"},
      /* 103 */ {"var103", "undefined"},
      /* 104 */ {"var104", "undefined"},
      /* 105 */ {"var105", "undefined"},
      /* 106 */ {"var106", "undefined"},
      /* 107 */ {"var107", "undefined"},
      /* 108 */ {"var108", "undefined"},
      /* 109 */ {"var109", "undefined"},
      /* 110 */ {"var110", "undefined"},
      /* 111 */ {"var111", "undefined"},
      /* 112 */ {"var112", "undefined"},
      /* 113 */ {"var113", "undefined"},
      /* 114 */ {"var114", "undefined"},
      /* 115 */ {"var115", "undefined"},
      /* 116 */ {"var116", "undefined"},
      /* 117 */ {"var117", "undefined"},
      /* 118 */ {"var118", "undefined"},
      /* 119 */ {"var119", "undefined"},
      /* 120 */ {"var120", "undefined"},
      /* 121 */ {"var121", "undefined"},
      /* 122 */ {"var122", "undefined"},
      /* 123 */ {"var123", "undefined"},
      /* 124 */ {"var124", "undefined"},
      /* 125 */ {"var125", "undefined"},
      /* 126 */ {"var126", "undefined"},
      /* 127 */ {"var127", "undefined"},
      /* 128 */ {"var128", "undefined"},
      /* 129 */ {"Z", "Geopotential [m**2 s**-2]"},
      /* 130 */ {"T", "Temperature [K]"},
      /* 131 */ {"U", "U velocity [m s**-1]"},
      /* 132 */ {"V", "V velocity [m s**-1]"},
      /* 133 */ {"Q", "Specific humidity [kg kg**-1]"},
      /* 134 */ {"var134", "undefined"},
      /* 135 */ {"var135", "undefined"},
      /* 136 */ {"var136", "undefined"},
      /* 137 */ {"var137", "undefined"},
      /* 138 */ {"var138", "undefined"},
      /* 139 */ {"STL1", "Soil temperature level 1 Surface temperature (ST) before 19930804 [K]"},
      /* 140 */ {"var140", "undefined"},
      /* 141 */ {"SD", "Snow depth [m of water]"},
      /* 142 */ {"var142", "undefined"},
      /* 143 */ {"var143", "undefined"},
      /* 144 */ {"var144", "undefined"},
      /* 145 */ {"var145", "undefined"},
      /* 146 */ {"var146", "undefined"},
      /* 147 */ {"var147", "undefined"},
      /* 148 */ {"var148", "undefined"},
      /* 149 */ {"var149", "undefined"},
      /* 150 */ {"var150", "undefined"},
      /* 151 */ {"MSL", "Mean sea level pressure [Pa]"},
      /* 152 */ {"var152", "undefined"},
      /* 153 */ {"var153", "undefined"},
      /* 154 */ {"var154", "undefined"},
      /* 155 */ {"var155", "undefined"},
      /* 156 */ {"var156", "undefined"},
      /* 157 */ {"var157", "undefined"},
      /* 158 */ {"var158", "undefined"},
      /* 159 */ {"var159", "undefined"},
      /* 160 */ {"var160", "undefined"},
      /* 161 */ {"var161", "undefined"},
      /* 162 */ {"var162", "undefined"},
      /* 163 */ {"var163", "undefined"},
      /* 164 */ {"TCC", "Total cloud cover [0 - 1]"},
      /* 165 */ {"10U", "10 metre U wind component [m s**-1]"},
      /* 166 */ {"10V", "10 metre V wind component [m s**-1]"},
      /* 167 */ {"2T", "2 metre temperature [K]"},
      /* 168 */ {"2D", "2 metre dewpoint temperature [K]"},
      /* 169 */ {"SSRD", "Downward surface solar radiation [W s m**-2]"},
      /* 170 */ {"CAP", "Field capacity [?]"},
      /* 171 */ {"WILT", "Wilting points [?]"},
      /* 172 */ {"LSM", "Land-sea mask [0-1]"},
      /* 173 */ {"SR", "Surface roughness length [0-1]"},
      /* 174 */ {"AL", "Albedo [0-1]"},
      /* 175 */ {"STRD", "Downward surface long wave radiation [W s m**-2]"},
      /* 176 */ {"SSR", "Surface net solar radiation [W s m**-2]"},
      /* 177 */ {"STR", "Surface net longwave radiation [W s m**-2]"},
      /* 178 */ {"TSR", "Top net solar radiation [W s m**-2]"},
      /* 179 */ {"TTR", "Top net longwave radiation [W s m**-2]"},
      /* 180 */ {"var180", "undefined"},
      /* 181 */ {"var181", "undefined"},
      /* 182 */ {"var182", "undefined"},
      /* 183 */ {"var183", "undefined"},
      /* 184 */ {"var184", "undefined"},
      /* 185 */ {"var185", "undefined"},
      /* 186 */ {"var186", "undefined"},
      /* 187 */ {"var187", "undefined"},
      /* 188 */ {"var188", "undefined"},
      /* 189 */ {"var189", "undefined"},
      /* 190 */ {"var190", "undefined"},
      /* 191 */ {"var191", "undefined"},
      /* 192 */ {"var192", "undefined"},
      /* 193 */ {"var193", "undefined"},
      /* 194 */ {"var194", "undefined"},
      /* 195 */ {"var195", "undefined"},
      /* 196 */ {"var196", "undefined"},
      /* 197 */ {"var197", "undefined"},
      /* 198 */ {"var198", "undefined"},
      /* 199 */ {"var199", "undefined"},
      /* 200 */ {"var200", "undefined"},
      /* 201 */ {"TMAX", "Maximum temperature [K]"},
      /* 202 */ {"TMIN", "Minimum temperature [K]"},
      /* 203 */ {"var203", "undefined"},
      /* 204 */ {"var204", "undefined"},
      /* 205 */ {"var205", "undefined"},
      /* 206 */ {"var206", "undefined"},
      /* 207 */ {"var207", "undefined"},
      /* 208 */ {"var208", "undefined"},
      /* 209 */ {"var209", "undefined"},
      /* 210 */ {"var210", "undefined"},
      /* 211 */ {"var211", "undefined"},
      /* 212 */ {"var212", "undefined"},
      /* 213 */ {"var213", "undefined"},
      /* 214 */ {"var214", "undefined"},
      /* 215 */ {"var215", "undefined"},
      /* 216 */ {"var216", "undefined"},
      /* 217 */ {"var217", "undefined"},
      /* 218 */ {"var218", "undefined"},
      /* 219 */ {"var219", "undefined"},
      /* 220 */ {"var220", "undefined"},
      /* 221 */ {"var221", "undefined"},
      /* 222 */ {"var222", "undefined"},
      /* 223 */ {"var223", "undefined"},
      /* 224 */ {"var224", "undefined"},
      /* 225 */ {"var225", "undefined"},
      /* 226 */ {"var226", "undefined"},
      /* 227 */ {"var227", "undefined"},
      /* 228 */ {"TP", "Total precipitation [m]"},
      /* 229 */ {"TSM", "Total soil moisture [m (kg?)]"},
      /* 230 */ {"var230", "undefined"},
      /* 231 */ {"var231", "undefined"},
      /* 232 */ {"var232", "undefined"},
      /* 233 */ {"var233", "undefined"},
      /* 234 */ {"var234", "undefined"},
      /* 235 */ {"var235", "undefined"},
      /* 236 */ {"var236", "undefined"},
      /* 237 */ {"var237", "undefined"},
      /* 238 */ {"var238", "undefined"},
      /* 239 */ {"var239", "undefined"},
      /* 240 */ {"var240", "undefined"},
      /* 241 */ {"var241", "undefined"},
      /* 242 */ {"var242", "undefined"},
      /* 243 */ {"var243", "undefined"},
      /* 244 */ {"var244", "undefined"},
      /* 245 */ {"var245", "undefined"},
      /* 246 */ {"var246", "undefined"},
      /* 247 */ {"var247", "undefined"},
      /* 248 */ {"var248", "undefined"},
      /* 249 */ {"var249", "undefined"},
      /* 250 */ {"var250", "undefined"},
      /* 251 */ {"var251", "undefined"},
      /* 252 */ {"var252", "undefined"},
      /* 253 */ {"var253", "undefined"},
      /* 254 */ {"var254", "undefined"},
      /* 255 */ {"var255", "undefined"},
};

struct ParmTable parm_table_nceptab_129[256] = {
      /* 0 */ {"var0", "undefined"},
      /* 1 */ {"PRES", "Pressure [Pa]"},
      /* 2 */ {"PRMSL", "Pressure reduced to MSL [Pa]"},
      /* 3 */ {"PTEND", "Pressure tendency [Pa/s]"},
      /* 4 */ {"PVORT", "Pot. vorticity [km^2/kg/s]"},
      /* 5 */ {"ICAHT", "ICAO Standard Atmosphere Reference Height [M]"},
      /* 6 */ {"GP", "Geopotential [m^2/s^2]"},
      /* 7 */ {"HGT", "Geopotential height [gpm]"},
      /* 8 */ {"DIST", "Geometric height [m]"},
      /* 9 */ {"HSTDV", "Std dev of height [m]"},
      /* 10 */ {"TOZNE", "Total ozone [Dobson]"},
      /* 11 */ {"TMP", "Temp. [K]"},
      /* 12 */ {"VTMP", "Virtual temp. [K]"},
      /* 13 */ {"POT", "Potential temp. [K]"},
      /* 14 */ {"EPOT", "Pseudo-adiabatic pot. temp. [K]"},
      /* 15 */ {"TMAX", "Max. temp. [K]"},
      /* 16 */ {"TMIN", "Min. temp. [K]"},
      /* 17 */ {"DPT", "Dew point temp. [K]"},
      /* 18 */ {"DEPR", "Dew point depression [K]"},
      /* 19 */ {"LAPR", "Lapse rate [K/m]"},
      /* 20 */ {"VIS", "Visibility [m]"},
      /* 21 */ {"RDSP1", "Radar spectra (1) [non-dim]"},
      /* 22 */ {"RDSP2", "Radar spectra (2) [non-dim]"},
      /* 23 */ {"RDSP3", "Radar spectra (3) [non-dim]"},
      /* 24 */ {"PLI", "Parcel lifted index (to 500 hPa) [K]"},
      /* 25 */ {"TMPA", "Temp. anomaly [K]"},
      /* 26 */ {"PRESA", "Pressure anomaly [Pa]"},
      /* 27 */ {"GPA", "Geopotential height anomaly [gpm]"},
      /* 28 */ {"WVSP1", "Wave spectra (1) [non-dim]"},
      /* 29 */ {"WVSP2", "Wave spectra (2) [non-dim]"},
      /* 30 */ {"WVSP3", "Wave spectra (3) [non-dim]"},
      /* 31 */ {"WDIR", "Wind direction [deg]"},
      /* 32 */ {"WIND", "Wind speed [m/s]"},
      /* 33 */ {"UGRD", "u wind [m/s]"},
      /* 34 */ {"VGRD", "v wind [m/s]"},
      /* 35 */ {"STRM", "Stream function [m^2/s]"},
      /* 36 */ {"VPOT", "Velocity potential [m^2/s]"},
      /* 37 */ {"MNTSF", "Montgomery stream function [m^2/s^2]"},
      /* 38 */ {"SGCVV", "Sigma coord. vertical velocity [/s]"},
      /* 39 */ {"VVEL", "Pressure vertical velocity [Pa/s]"},
      /* 40 */ {"DZDT", "Geometric vertical velocity [m/s]"},
      /* 41 */ {"ABSV", "Absolute vorticity [/s]"},
      /* 42 */ {"ABSD", "Absolute divergence [/s]"},
      /* 43 */ {"RELV", "Relative vorticity [/s]"},
      /* 44 */ {"RELD", "Relative divergence [/s]"},
      /* 45 */ {"VUCSH", "Vertical u shear [/s]"},
      /* 46 */ {"VVCSH", "Vertical v shear [/s]"},
      /* 47 */ {"DIRC", "Direction of current [deg]"},
      /* 48 */ {"SPC", "Speed of current [m/s]"},
      /* 49 */ {"UOGRD", "u of current [m/s]"},
      /* 50 */ {"VOGRD", "v of current [m/s]"},
      /* 51 */ {"SPFH", "Specific humidity [kg/kg]"},
      /* 52 */ {"RH", "Relative humidity [%]"},
      /* 53 */ {"MIXR", "Humidity mixing ratio [kg/kg]"},
      /* 54 */ {"PWAT", "Precipitable water [kg/m^2]"},
      /* 55 */ {"VAPP", "Vapor pressure [Pa]"},
      /* 56 */ {"SATD", "Saturation deficit [Pa]"},
      /* 57 */ {"EVP", "Evaporation [kg/m^2]"},
      /* 58 */ {"CICE", "Cloud Ice [kg/m^2]"},
      /* 59 */ {"PRATE", "Precipitation rate [kg/m^2/s]"},
      /* 60 */ {"TSTM", "Thunderstorm probability [%]"},
      /* 61 */ {"APCP", "Total precipitation [kg/m^2]"},
      /* 62 */ {"NCPCP", "Large scale precipitation [kg/m^2]"},
      /* 63 */ {"ACPCP", "Convective precipitation [kg/m^2]"},
      /* 64 */ {"SRWEQ", "Snowfall rate water equiv. [kg/m^2/s]"},
      /* 65 */ {"WEASD", "Accum. snow [kg/m^2]"},
      /* 66 */ {"SNOD", "Snow depth [m]"},
      /* 67 */ {"MIXHT", "Mixed layer depth [m]"},
      /* 68 */ {"TTHDP", "Transient thermocline depth [m]"},
      /* 69 */ {"MTHD", "Main thermocline depth [m]"},
      /* 70 */ {"MTHA", "Main thermocline anomaly [m]"},
      /* 71 */ {"TCDC", "Total cloud cover [%]"},
      /* 72 */ {"CDCON", "Convective cloud cover [%]"},
      /* 73 */ {"LCDC", "Low level cloud cover [%]"},
      /* 74 */ {"MCDC", "Mid level cloud cover [%]"},
      /* 75 */ {"HCDC", "High level cloud cover [%]"},
      /* 76 */ {"CWAT", "Cloud water [kg/m^2]"},
      /* 77 */ {"BLI", "Best lifted index (to 500 hPa) [K]"},
      /* 78 */ {"SNOC", "Convective snow [kg/m^2]"},
      /* 79 */ {"SNOL", "Large scale snow [kg/m^2]"},
      /* 80 */ {"WTMP", "Water temp. [K]"},
      /* 81 */ {"LAND", "Land cover (land=1;sea=0) [fraction]"},
      /* 82 */ {"DSLM", "Deviation of sea level from mean [m]"},
      /* 83 */ {"SFCR", "Surface roughness [m]"},
      /* 84 */ {"ALBDO", "Albedo [%]"},
      /* 85 */ {"TSOIL", "Soil temp. [K]"},
      /* 86 */ {"SOILM", "Soil moisture content [kg/m^2]"},
      /* 87 */ {"VEG", "Vegetation [%]"},
      /* 88 */ {"SALTY", "Salinity [kg/kg]"},
      /* 89 */ {"DEN", "Density [kg/m^3]"},
      /* 90 */ {"WATR", "Water runoff [kg/m^2]"},
      /* 91 */ {"ICEC", "Ice concentration (ice=1;no ice=0) [fraction]"},
      /* 92 */ {"ICETK", "Ice thickness [m]"},
      /* 93 */ {"DICED", "Direction of ice drift [deg]"},
      /* 94 */ {"SICED", "Speed of ice drift [m/s]"},
      /* 95 */ {"UICE", "u of ice drift [m/s]"},
      /* 96 */ {"VICE", "v of ice drift [m/s]"},
      /* 97 */ {"ICEG", "Ice growth rate [m/s]"},
      /* 98 */ {"ICED", "Ice divergence [/s]"},
      /* 99 */ {"SNOM", "Snow melt [kg/m^2]"},
      /* 100 */ {"HTSGW", "Sig height of wind waves and swell [m]"},
      /* 101 */ {"WVDIR", "Direction of wind waves [deg]"},
      /* 102 */ {"WVHGT", "Sig height of wind waves [m]"},
      /* 103 */ {"WVPER", "Mean period of wind waves [s]"},
      /* 104 */ {"SWDIR", "Direction of swell waves [deg]"},
      /* 105 */ {"SWELL", "Sig height of swell waves [m]"},
      /* 106 */ {"SWPER", "Mean period of swell waves [s]"},
      /* 107 */ {"DIRPW", "Primary wave direction [deg]"},
      /* 108 */ {"PERPW", "Primary wave mean period [s]"},
      /* 109 */ {"DIRSW", "Secondary wave direction [deg]"},
      /* 110 */ {"PERSW", "Secondary wave mean period [s]"},
      /* 111 */ {"NSWRS", "Net short wave (surface) [W/m^2]"},
      /* 112 */ {"NLWRS", "Net long wave (surface) [W/m^2]"},
      /* 113 */ {"NSWRT", "Net short wave (top) [W/m^2]"},
      /* 114 */ {"NLWRT", "Net long wave (top) [W/m^2]"},
      /* 115 */ {"LWAVR", "Long wave [W/m^2]"},
      /* 116 */ {"SWAVR", "Short wave [W/m^2]"},
      /* 117 */ {"GRAD", "Global radiation [W/m^2]"},
      /* 118 */ {"BRTMP", "Brightness temperature [K]"},
      /* 119 */ {"LWRAD", "Radiance with respect to wave no. [W/m/sr]"},
      /* 120 */ {"SWRAD", "Radiance with respect ot wave len. [W/m^3/sr]"},
      /* 121 */ {"LHTFL", "Latent heat flux [W/m^2]"},
      /* 122 */ {"SHTFL", "Sensible heat flux [W/m^2]"},
      /* 123 */ {"BLYDP", "Boundary layer dissipation [W/m^2]"},
      /* 124 */ {"UFLX", "Zonal momentum flux [N/m^2]"},
      /* 125 */ {"VFLX", "Meridional momentum flux [N/m^2]"},
      /* 126 */ {"WMIXE", "Wind mixing energy [J]"},
      /* 127 */ {"IMGD", "Image data []"},
      /* 128 */ {"PAOT", "Probability anomaly of temp [%]"},
      /* 129 */ {"PAOP", "Probability anomaly of precip [%]"},
      /* 130 */ {"var130", "undefined"},
      /* 131 */ {"FRAIN", "Rain fraction of total liquid water []"},
      /* 132 */ {"FICE", "Ice fraction of total condensate []"},
      /* 133 */ {"FRIME", "Rime factor []"},
      /* 134 */ {"CUEFI", "Convective cloud efficiency []"},
      /* 135 */ {"TCOND", "Total condensate [kg/kg]"},
      /* 136 */ {"TCOLW", "Total column cloud water [kg/m/m]"},
      /* 137 */ {"TCOLI", "Total column cloud ice [kg/m/m]"},
      /* 138 */ {"TCOLR", "Total column rain [kg/m/m]"},
      /* 139 */ {"TCOLS", "Total column snow [kg/m/m]"},
      /* 140 */ {"TCOLC", "Total column condensate [kg/m/m]"},
      /* 141 */ {"PLPL", "Pressure of level from which parcel was lifted [Pa]"},
      /* 142 */ {"HLPL", "Height of level from which parcel was lifted [m]"},
      /* 143 */ {"CEMS", "Cloud Emissivity [fraction]"},
      /* 144 */ {"COPD", "Cloud Optical Depth [non-dim]"},
      /* 145 */ {"PSIZ", "Effective Particle size [microns]"},
      /* 146 */ {"TCWAT", "Total Water Cloud [%]"},
      /* 147 */ {"TCICE", "Total Ice Cloud [%]"},
      /* 148 */ {"var148", "undefined"},
      /* 149 */ {"var149", "undefined"},
      /* 150 */ {"PTAN", "Probability of Temp. above normal [%]"},
      /* 151 */ {"PTNN", "Probability of Temp. near normal [%]"},
      /* 152 */ {"PTBN", "Probability of Temp. below normal [%]"},
      /* 153 */ {"PPAN", "Probability of Precip. above normal [%]"},
      /* 154 */ {"PPNN", "Probability of Precip. near normal [%]"},
      /* 155 */ {"PPBN", "Probability of Precip. below normal [%]"},
      /* 156 */ {"var156", "undefined"},
      /* 157 */ {"var157", "undefined"},
      /* 158 */ {"var158", "undefined"},
      /* 159 */ {"var159", "undefined"},
      /* 160 */ {"var160", "undefined"},
      /* 161 */ {"var161", "undefined"},
      /* 162 */ {"var162", "undefined"},
      /* 163 */ {"var163", "undefined"},
      /* 164 */ {"var164", "undefined"},
      /* 165 */ {"var165", "undefined"},
      /* 166 */ {"var166", "undefined"},
      /* 167 */ {"var167", "undefined"},
      /* 168 */ {"var168", "undefined"},
      /* 169 */ {"var169", "undefined"},
      /* 170 */ {"ELRDI", "Ellrod Index"},
      /* 171 */ {"TSEC", "Seconds prior to initial reference time [sec]"},
      /* 172 */ {"TSECA", "Seconds after initial reference time [sec]"},
      /* 173 */ {"NUM", "Number of samples/observations [non-dim]j"},
      /* 174 */ {"var174", "undefined"},
      /* 175 */ {"var175", "undefined"},
      /* 176 */ {"var176", "undefined"},
      /* 177 */ {"var177", "undefined"},
      /* 178 */ {"var178", "undefined"},
      /* 179 */ {"var179", "undefined"},
      /* 180 */ {"OZCON", "Ozone concentration [ppb]"},
      /* 181 */ {"OZCAT", "Categorical ozone concentration [?]"},
      /* 182 */ {"KH", "vertical heat eddy diffusivity [m^2/s]"},
      /* 183 */ {"SIGV", "Sigma level value ?? [non-dim]"},
      /* 184 */ {"var184", "undefined"},
      /* 185 */ {"var185", "undefined"},
      /* 186 */ {"var186", "undefined"},
      /* 187 */ {"var187", "undefined"},
      /* 188 */ {"var188", "undefined"},
      /* 189 */ {"var189", "undefined"},
      /* 190 */ {"USCT", "Scatterometer est. U wind component [m/s]"},
      /* 191 */ {"VSCT", "Scatterometer est. V wind component [m/s]"},
      /* 192 */ {"var192", "undefined"},
      /* 193 */ {"var193", "undefined"},
      /* 194 */ {"var194", "undefined"},
      /* 195 */ {"DBSS", "Geometric Depth Below Sea Surface [m]"},
      /* 196 */ {"ODHA", "Ocean Dynamic Heat Anomaly [dynamic m]"},
      /* 197 */ {"OHC", "Ocean Heat Content [J/m^2]"},
      /* 198 */ {"SSHG", "Sea Surface Height Relative to Geoid [m]"},
      /* 199 */ {"SLTFL", "Salt flux [g/cm^2/s]"},
      /* 200 */ {"DUVB", "UV-B Downward Solar Flux [W/m^2]"},
      /* 201 */ {"CDUVB", "Clear Sky UV-B Downward Solar Flux [W/m^2]"},
      /* 202 */ {"THFLX", "Total downward heat flux at surface [W/m^2]"},
      /* 203 */ {"var203", "undefined"},
      /* 204 */ {"var204", "undefined"},
      /* 205 */ {"var205", "undefined"},
      /* 206 */ {"var206", "undefined"},
      /* 207 */ {"var207", "undefined"},
      /* 208 */ {"var208", "undefined"},
      /* 209 */ {"var209", "undefined"},
      /* 210 */ {"REFO", "Observed radar reflectivity [dbZ]"},
      /* 211 */ {"REFD", "Derived radar reflectivity [dbZ]"},
      /* 212 */ {"REFC", "Maximum/Composite radar reflectivity [dbZ]"},
      /* 213 */ {"var213", "undefined"},
      /* 214 */ {"var214", "undefined"},
      /* 215 */ {"var215", "undefined"},
      /* 216 */ {"var216", "undefined"},
      /* 217 */ {"var217", "undefined"},
      /* 218 */ {"var218", "undefined"},
      /* 219 */ {"var219", "undefined"},
      /* 220 */ {"var220", "undefined"},
      /* 221 */ {"var221", "undefined"},
      /* 222 */ {"var222", "undefined"},
      /* 223 */ {"var223", "undefined"},
      /* 224 */ {"var224", "undefined"},
      /* 225 */ {"var225", "undefined"},
      /* 226 */ {"var226", "undefined"},
      /* 227 */ {"var227", "undefined"},
      /* 228 */ {"var228", "undefined"},
      /* 229 */ {"var229", "undefined"},
      /* 230 */ {"var230", "undefined"},
      /* 231 */ {"var231", "undefined"},
      /* 232 */ {"var232", "undefined"},
      /* 233 */ {"var233", "undefined"},
      /* 234 */ {"var234", "undefined"},
      /* 235 */ {"var235", "undefined"},
      /* 236 */ {"var236", "undefined"},
      /* 237 */ {"var237", "undefined"},
      /* 238 */ {"var238", "undefined"},
      /* 239 */ {"var239", "undefined"},
      /* 240 */ {"var240", "undefined"},
      /* 241 */ {"var241", "undefined"},
      /* 242 */ {"var242", "undefined"},
      /* 243 */ {"var243", "undefined"},
      /* 244 */ {"var244", "undefined"},
      /* 245 */ {"var245", "undefined"},
      /* 246 */ {"var246", "undefined"},
      /* 247 */ {"var247", "undefined"},
      /* 248 */ {"var248", "undefined"},
      /* 249 */ {"var249", "undefined"},
      /* 250 */ {"var250", "undefined"},
      /* 251 */ {"var251", "undefined"},
      /* 252 */ {"var252", "undefined"},
      /* 253 */ {"var253", "undefined"},
      /* 254 */ {"var254", "undefined"},
      /* 255 */ {"var255", "undefined"},
};

/* parameter table for ocean modeling branch (OMB) of NCEP */
/* center = 7, subcenter = EMC, parameter table = 128 */

/* 12/31/2001 added REV */

struct ParmTable parm_table_omb[256] = {
 {"var0", "Reserved"},
 {"var1", "Reserved"},
 {"GHz6", "6.6 GHz - K"},
 {"GHz10", "10.7 GHz - K"},
 {"GHz18", "18.0 GHz - K"},
 {"GHz19V", "SSMI 19 GHz, Vertical Polarization - K"},
 {"GHz19H", "SSMI 19 GHz, Horizontal Polarization - K"},
 {"GHz21", "21.0 GHz - K"},
 {"GHz22V", "SSMI 22 GHz, Vertical Polarization - K"},
 {"GHz37V", "SSMI 37 GHz, Vertical Polarization - K"},
 {"GHz37H", "SSMI 37 GHz, Horizontal Polarization - K"},
 {"MSU1", "MSU Ch 1 - 50.30 GHz - K"},
 {"MSU2", "MSU Ch 2 - 53.74 GHz - K"},
 {"MSU3", "MSU Ch 3 - 54.96 GHz - K"},
 {"MSU4", "MSU Ch 4 - 57.95 GHz - K"},
 {"GHz85V", "SSMI 85 GHz, Vertical Polarization - K"},
 {"GHz85H", "SSMI 85 GHz, Horizontal Polarization - K"},
 {"GHz91", "91.65 GHz - K"},
 {"GHz150", "150 GHz - K"},
 {"GHz183pm7", "183 +- 7 GHz - K"},
 {"GHz183pm3", "183 +- 3 GHz - K"},
 {"GHz183pm1", "183 +- 1 GHz - K"},
 {"SSMT1C1", "SSM/T1 - ch 1 - K"},
 {"SSMT1C2", "SSM/T1 - ch 2 - K"},
 {"SSMT1C3", "SSM/T1 - ch 3 - K"},
 {"SSMT1C4", "SSM/T1 - ch 4 - K"},
 {"SSMT1C5", "SSM/T1 - ch 5 - K"},
 {"SSMT1C6", "SSM/T1 - ch 6 - K"},
 {"SSMT1C7", "SSM/T1 - ch 7 - K"},
 {"var29", "Reserved"},
 {"var30", "Reserved"},
 {"var31", "Reserved"},
 {"var32", "Reserved"},
 {"var33", "Reserved"},
 {"var34", "Reserved"},
 {"var35", "Reserved"},
 {"var36", "Reserved"},
 {"var37", "Reserved"},
 {"var38", "Reserved"},
 {"var39", "Reserved"},
 {"var40", "Reserved"},
 {"var41", "Reserved"},
 {"var42", "Reserved"},
 {"var43", "Reserved"},
 {"var44", "Reserved"},
 {"var45", "Reserved"},
 {"var46", "Reserved"},
 {"var47", "Reserved"},
 {"var48", "Reserved"},
 {"var49", "Reserved"},
 {"var50", "Reserved"},
 {"var51", "Reserved"},
 {"var52", "Reserved"},
 {"var53", "Reserved"},
 {"var54", "Reserved"},
 {"var55", "Reserved"},
 {"var56", "Reserved"},
 {"var57", "Reserved"},
 {"var58", "Reserved"},
 {"var59", "Reserved"},
 {"MI14.95", "HIRS/2 ch 1 - 14.95 micron - K"},
 {"MI14.71", "HIRS/2, GOES 14.71 micron - K"},
 {"MI14.49", "HIRS/2 ch 3 - 14.49 micron - K"},
 {"MI14.37", "GOES I-M - 14.37 micron - K"},
 {"MI14.22", "HIRS/2 ch 4 - 14.22 micron - K"},
 {"MI14.06", "GOES I-M - 14.06 micron - K"},
 {"MI13.97", "HIRS/2 ch 5 - 13.97 micron - K"},
 {"MI13.64", "HIRS/2, GOES 13.64 micron - K"},
 {"MI13.37", "GOES I-M - 13.37 micron - K"},
 {"MI13.35", "HIRS/2 ch 7 - 13.35 micron - K"},
 {"MI12.66", "GOES I-M - 12.66 micron - K"},
 {"MI12.02", "GOES I-M - 12.02 micron - K"},
 {"MI12.00", "AVHRR ch 5 - 12.0 micron - K"},
 {"MI11.11", "HIRS/2 ch 8 - 11.11 micron - K"},
 {"MI11.03", "GOES I-M - 11.03 micron - K"},
 {"MI10.80", "AVHRR ch 4 - 10.8 micron - K"},
 {"MI9.71", "HIRS/2, GOES - 9.71 micron - K"},
 {"var77", "Reserved"},
 {"var78", "Reserved"},
 {"var79", "Reserved"},
 {"MI8.16", "HIRS/2 ch 10 - 8.16 micron - K"},
 {"MI7.43", "GOES I-M - 7.43 micron - K"},
 {"MI7.33", "HIRS/2 ch 11 - 7.33 micron - K"},
 {"MI7.02", "GOES I-M - 7.02 micron - K"},
 {"MI6.72", "HIRS/2 ch 12 - 6.72 micron - K"},
 {"MI6.51", "GOES I-M - 6.51 micron - K"},
 {"MI4.57", "HIRS/2, GOES - 4.57 micron - K"},
 {"MI4.52", "HIRS/2, GOES - 4.52 micron - K"},
 {"MI4.46", "HIRS/2 ch 15 - 4.46 micron - K"},
 {"MI4.45", "GOES I-M - 4.45 micron - K"},
 {"MI4.40", "HIRS/2 ch 16 - 4.40 micron - K"},
 {"MI4.24", "HIRS/2 ch 17 - 4.24 micron - K"},
 {"MI4.13", "GOES I-M - 4.13 micron - K"},
 {"MI4.00", "HIRS/2 ch 18 - 4.00 micron - K"},
 {"MI8.16", "GOES I-M - 3.98 micron - K"},
 {"MI8.16", "HIRS/2 Window - 3.76 micron - K"},
 {"MI8.16", "AVHRR, GOES - 3.74 micron - K"},
 {"var97", "Reserved"},
 {"var98", "Reserved"},
 {"var99", "Reserved"},
 {"MI0.91", "AVHRR ch 2 - 0.91 micron - K"},
 {"MI0.696", "GOES I-M - 0.696 micron - K"},
 {"MI0.69", "HIRS/2 Vis - 0.69 micron - K"},
 {"MI0.63", "AVHRR ch 1 - 0.63 micron - K"},
 {"var104", "Reserved"},
 {"var105", "Reserved"},
 {"var106", "Reserved"},
 {"var107", "Reserved"},
 {"var108", "Reserved"},
 {"var109", "Reserved"},
 {"var110", "Reserved"},
 {"var111", "Reserved"},
 {"var112", "Reserved"},
 {"var113", "Reserved"},
 {"var114", "Reserved"},
 {"var115", "Reserved"},
 {"var116", "Reserved"},
 {"var117", "Reserved"},
 {"var118", "Reserved"},
 {"var119", "Reserved"},
 {"var120", "Reserved"},
 {"var121", "Reserved"},
 {"var122", "Reserved"},
 {"var123", "Reserved"},
 {"var124", "Reserved"},
 {"var125", "Reserved"},
 {"var126", "Reserved"},
 {"var127", "Reserved"},
 {"AVDEPTH", "Ocean depth - mean - m"},
 {"DEPTH", "Ocean depth - instantaneous - m"},
 {"ELEV", "Ocean surface elevation relative to geoid - m"},
 {"MXEL24", "Max ocean surface elevation in last 24 hours - m"},
 {"MNEL24", "Min ocean surface elevation in last 24 hours - m"},
 {"var133", "Reserved"},
 {"var134", "Reserved"},
 {"O2", "Oxygen -Mol/kg"},
 {"PO4", "PO4 - Mol/kg"},
 {"NO3", "NO3 - Mol/kg"},
 {"SiO4", "SiO4 - Mol/kg"},
 {"CO2aq", "CO2 (aq) - Mol/kg"},
 {"HCO3", "HCO3 - - Mol/kg"},
 {"CO3", "CO3 -- - Mol/kg"},
 {"TCO2", "TCO2 - Mol/kg"},
 {"TALK", "TALK - Mol/kg"},
 {"var144", "Reserved"},
 {"var145", "Reserved"},
 {"S11", "S11 - 1,1 component of ice stress tensor"},
 {"S12", "S12 - 1,2 component of ice stress tensor"},
 {"S22", "S22 - 2,2 component of ice stress tensor"},
 {"INV1", "T1 - First invariant of stress tensor"},
 {"INV2", "T2 - Second invariant of stress tensor"},
 {"var151", "Reserved"},
 {"var152", "Reserved"},
 {"var153", "Reserved"},
 {"var154", "Reserved"},
 {"WVRGH", "Wave Roughness"},
 {"WVSTRS", "Wave Stresses"},
 {"WHITE", "Whitecap coverage"},
 {"SWDIRWID", "Swell direction width"},
 {"SWFREWID", "Swell frequency width"},
 {"WVAGE", "Wave age"},
 {"PWVAGE", "Physical Wave age"},
 {"var162", "Reserved"},
 {"var163", "Reserved"},
 {"var164", "Reserved"},
 {"LTURB", "Master length scale (turbulence) - m"},
 {"var166", "Reserved"},
 {"var167", "Reserved"},
 {"var168", "Reserved"},
 {"var169", "Reserved"},
 {"AIHFLX", "Net Air-Ice heat flux - W/m^2"},
 {"AOHFLX", "Net Air-Ocean heat flux - W/m^2"},
 {"IOHFLX", "Net Ice-Ocean heat flux - W/m^2"},
 {"IOSFLX", "Net Ice-Ocean salt flux - kg/s"},
 {"var174", "Reserved"},
 {"OMLT", "Ocean Mixed Layer Temperature - K"},
 {"OMLS", "Ocean Mixed Layer Salinity - kg/kg"},
 {"var177", "Reserved"},
 {"var178", "Reserved"},
 {"var179", "Reserved"},
 {"var180", "Reserved"},
 {"var181", "Reserved"},
 {"var182", "Reserved"},
 {"var183", "Reserved"},
 {"var184", "Reserved"},
 {"var185", "Reserved"},
 {"var186", "Reserved"},
 {"var187", "Reserved"},
 {"var188", "Reserved"},
 {"var189", "Reserved"},
 {"var190", "Reserved"},
 {"var191", "Reserved"},
 {"var192", "Reserved"},
 {"var193", "Reserved"},
 {"var194", "Reserved"},
 {"var195", "Reserved"},
 {"var196", "Reserved"},
 {"var197", "Reserved"},
 {"var198", "Reserved"},
 {"var199", "Reserved"},
 {"var200", "Reserved"},
 {"var201", "Reserved"},
 {"var202", "Reserved"},
 {"var203", "Reserved"},
 {"var204", "Reserved"},
 {"var205", "Reserved"},
 {"var206", "Reserved"},
 {"var207", "Reserved"},
 {"var208", "Reserved"},
 {"var209", "Reserved"},
 {"var210", "Reserved"},
 {"var211", "Reserved"},
 {"var212", "Reserved"},
 {"var213", "Reserved"},
 {"var214", "Reserved"},
 {"var215", "Reserved"},
 {"var216", "Reserved"},
 {"var217", "Reserved"},
 {"var218", "Reserved"},
 {"var219", "Reserved"},
 {"var220", "Reserved"},
 {"var221", "Reserved"},
 {"var222", "Reserved"},
 {"var223", "Reserved"},
 {"var224", "Reserved"},
 {"var225", "Reserved"},
 {"var226", "Reserved"},
 {"var227", "Reserved"},
 {"var228", "Reserved"},
 {"var229", "Reserved"},
 {"var230", "Reserved"},
 {"var231", "Reserved"},
 {"var232", "Reserved"},
 {"var233", "Reserved"},
 {"var234", "Reserved"},
 {"var235", "Reserved"},
 {"var236", "Reserved"},
 {"var237", "Reserved"},
 {"var238", "Reserved"},
 {"var239", "Reserved"},
 {"var240", "Reserved"},
 {"var241", "Reserved"},
 {"var242", "Reserved"},
 {"var243", "Reserved"},
 {"var244", "Reserved"},
 {"var245", "Reserved"},
 {"var246", "Reserved"},
 {"var247", "Reserved"},
 {"var248", "Reserved"},
 {"var249", "Reserved"},
 {"var250", "Reserved"},
 {"var251", "Reserved"},
 {"var252", "Reserved"},
 {"var253", "Reserved"},
 {"REV", "Relative Error Variance [non-dim]"},
 {"var255", "Reserved"}
};


/*
 * EC_ext	v1.0 wesley ebisuzaki
 *
 * prints something readable from the EC stream parameter
 *
 * prefix and suffix are only printed if EC_ext has text
 */

void EC_ext(unsigned char *pds, char *prefix, char *suffix, int verbose) {

    int local_id, ec_type, ec_class, ec_stream;
    char string[200];

    if (PDS_Center(pds) != ECMWF) return;

    local_id = PDS_EcLocalId(pds);
    if (local_id  == 0) return;
    ec_class = PDS_EcClass(pds);
    ec_type = PDS_EcType(pds);
    ec_stream = PDS_EcStream(pds);

    if (verbose == 2) printf("%sECext=%d%s", prefix, local_id, suffix);

    if (verbose == 2) {
	switch(ec_class) {
	    case 1: strcpy(string, "operations"); break;
	    case 2: strcpy(string, "research"); break;
	    case 3: strcpy(string, "ERA-15"); break;
	    case 4: strcpy(string, "Euro clim support network"); break;
	    case 5: strcpy(string, "ERA-40"); break;
	    case 6: strcpy(string, "DEMETER"); break;
	    case 7: strcpy(string, "PROVOST"); break;
	    case 8: strcpy(string, "ELDAS"); break;
	     default: sprintf(string, "%d", ec_class); break;
	}
        printf("%sclass=%s%s",prefix,string,suffix);
    }
    /*
     10/03/2000: R.Rudsar : subroutine changed.
                 Tests for EcType and extra test for EcStream 1035
    */


    if (verbose == 2) {
        switch(ec_type) {
            case 1: strcpy(string, "first guess"); break;
            case 2: strcpy(string, "analysis"); break;
            case 3: strcpy(string, "init analysis"); break;
            case 4: strcpy(string, "OI analysis"); break;
            case 10: strcpy(string, "Control forecast"); break;
            case 11: strcpy(string, "Perturbed forecasts"); break;
            case 14: strcpy(string, "Cluster means"); break;
            case 15: strcpy(string, "Cluster std. dev."); break;
            case 16: strcpy(string, "Forecast probability"); break;
            case 17: strcpy(string, "Ensemble means"); break;
            case 18: strcpy(string, "Ensemble std. dev."); break;
    	    case 20: strcpy(string, "Climatology"); break;
            case 21: strcpy(string, "Climatology simulation"); break;
            case 80: strcpy(string, "Fcst seasonal mean"); break;
            default: sprintf(string, "%d", ec_type); break;
        }
        printf("%stype=%s%s",prefix,string,suffix);
    }

    if (verbose == 2) {
        switch(ec_stream) {
	    case 1035: strcpy(string, "ensemble forecasts"); break;
	    case 1043: strcpy(string, "mon mean"); break;
	    case 1070: strcpy(string, "mon (co)var"); break;
	    case 1071: strcpy(string, "mon mean from daily"); break;
	    case 1090: strcpy(string, "EC ensemble fcsts"); break;
	    case 1091: strcpy(string, "EC seasonal fcst mon means"); break;
	    default:   sprintf(string, "%d", ec_stream); break;
        }
        printf("%sstream=%s%s",prefix,string,suffix);
    }
    if (verbose == 2) {
        printf("%sVersion=%c%c%c%c%s", prefix, *(PDS_Ec16Version(pds)), *(PDS_Ec16Version(pds)+1),
		*(PDS_Ec16Version(pds)+2), *(PDS_Ec16Version(pds)+3), suffix);
        if (local_id == 16) {
	    printf("%sSysVersion=%d%s", prefix, PDS_Ec16SysNum(pds), suffix);
	    printf("%sAvgPeriod=%d%s", prefix, PDS_Ec16AvePeriod(pds), suffix);
	    printf("%sFcstMon=%d%s", prefix, PDS_Ec16FcstMon(pds), suffix);

        }
    }

        if (local_id == 16) {
	    printf("%sEnsem_mem=%d%s", prefix, PDS_Ec16Number(pds), suffix);
	    printf("%sVerfDate=%d%s", prefix, PDS_Ec16VerfMon(pds), suffix);
        }

}

/*
 * get grid size from GDS
 *
 * added calculation of nxny of spectral data and clean up of triangular
 * grid nnxny calculation     l. kornblueh 
 * 7/25/03 wind fix Dusan Jovic
 * 9/17/03 fix scan mode
 */

int GDS_grid(unsigned char *gds, unsigned char *bds, int *nx, int *ny, 
             long int *nxny) {

    int i, d, ix, iy, pl;
    long int isum;

    *nx = ix = GDS_LatLon_nx(gds);
    *ny = iy = GDS_LatLon_ny(gds);
    *nxny = ix * iy;

    /* thin grid */

    if (GDS_Gaussian(gds) || GDS_LatLon(gds)) {
	if (ix == 65535) {
	    *nx = -1;
	    /* reduced grid */
	    isum = 0;
	    pl = GDS_PL(gds);
	    for (i = 0; i < iy; i++) {
		isum += gds[pl+i*2]*256 + gds[pl+i*2+1];
	    }
	    *nxny = isum;
	}
	return 0;
    }
    if (GDS_Triangular(gds)) {
        i = GDS_Triangular_ni(gds);
        d = GDS_Triangular_nd(gds);
	*nx = *nxny = d * (i + 1) * (i + 1);
        *ny = 1;
	return 0;
    }
    if (GDS_Harmonic(gds)) {
        /* this code assumes j, k, m are consistent with bds */
        *nx = *nxny = (8*(BDS_LEN(bds)-15)-BDS_UnusedBits(bds))/
		BDS_NumBits(bds)+1;
           if ((8*(BDS_LEN(bds)-15)-BDS_UnusedBits(bds)) % BDS_NumBits(bds)) {
	       fprintf(stderr,"inconsistent harmonic BDS\n");
           }
        *ny = 1;
    }
    return 0;
}

#define NCOL 15
void GDS_prt_thin_lon(unsigned char *gds) {
    int iy, i, col, pl;

    iy = GDS_LatLon_ny(gds);
    iy = (iy + 1) / 2;
    iy = GDS_LatLon_ny(gds);

    if ((pl = GDS_PL(gds)) == -1) {
	fprintf(stderr,"\nprogram error: GDS_prt_thin\n");
	return;
    }
    for (col = i = 0; i < iy; i++) {
	if (col == 0) printf("   ");
	printf("%5d", (gds[pl+i*2] << 8) + gds[pl+i*2+1]);
	col++;
	if (col == NCOL) {
	    col = 0;
	    printf("\n");
	}
    }
    if (col != 0) printf("\n");
}

/*
 * prints out wind rel to grid or earth
 */

static char *scan_mode[8] = {
	"WE:NS",
	"NS:WE",

	"WE:SN",
	"SN:WE",

        "EW:NS",
	"NS:EW",

	"EW:SN",
	"SN:EW" };


void GDS_winds(unsigned char *gds, int verbose) {
    int scan = -1, mode = -1;

    if (gds != NULL) {
        if (GDS_LatLon(gds)) {
	    scan = GDS_LatLon_scan(gds);
	    mode = GDS_LatLon_mode(gds);
	}
	else if (GDS_Mercator(gds)) {
	    scan =GDS_Merc_scan(gds);
	    mode =GDS_Merc_mode(gds);
	}
	/* else if (GDS_Gnomonic(gds)) { */
	else if (GDS_Lambert(gds)) {
	    scan = GDS_Lambert_scan(gds);
	    mode = GDS_Lambert_mode(gds);
	}
	else if (GDS_Gaussian(gds)) {
	    scan = GDS_LatLon_scan(gds);
	    mode = GDS_LatLon_mode(gds);
	}
	else if (GDS_Polar(gds)) {
	    scan = GDS_Polar_scan(gds);
	    mode = GDS_Polar_mode(gds);
	}
	else if (GDS_RotLL(gds)) {
	    scan = GDS_RotLL_scan(gds);
	    mode = GDS_RotLL_mode(gds);
	}
	/* else if (GDS_Triangular(gds)) { */
	else if (GDS_ssEgrid(gds)) {
	    scan = GDS_ssEgrid_scan(gds);
	    mode = GDS_ssEgrid_mode(gds);
	}
	else if (GDS_fEgrid(gds)) {
	    scan = GDS_fEgrid_scan(gds);
	    mode = GDS_fEgrid_mode(gds);
	}
	else if (GDS_ss2dEgrid(gds)) {
	    scan = GDS_ss2dEgrid_scan(gds);
	    mode = GDS_ss2dEgrid_mode(gds);
	}
    }
    if (verbose == 1) {
	if (mode != -1) {
	    if (mode & 8) printf("winds in grid direction:");
	    else printf("winds are N/S:"); 
	}
    }
    else if (verbose == 2) {
	if (scan != -1) {
	    printf(" scan: %s", scan_mode[(scan >> 5) & 7]);
        }
	if (mode != -1) {
	    if (mode & 8) printf(" winds(grid) ");
	    else printf(" winds(N/S) "); 
	}
    }
}



#define START -1

static int user_center = 0, user_subcenter = 0, user_ptable = 0;
static enum {filled, not_found, not_checked, no_file, init} status = init;

struct ParmTable parm_table_user[256];

/*
 * sets up user parameter table
 * v1.1 12/2005 w. ebisuzaki
 */

int setup_user_table(int center, int subcenter, int ptable) {

    int i, j, c0, c1, c2;
    static FILE *input;
    static int file_open = 0;
    char *filename, line[300];

    if (status == init) {
	for (i = 0; i < 256; i++) {
	    parm_table_user[i].name = parm_table_user[i].comment = NULL;
	}
	status = not_checked;
    }

    if (status == no_file) return 0;

    if ((user_center == -1 || center == user_center) &&
	    (user_subcenter == -1 || subcenter == user_subcenter) &&
	    (user_ptable == -1 || ptable == user_ptable)) {

	if (status == filled) return 1;
	if (status == not_found) return 0;
    }

    /* open gribtab file if not open */

    if (!file_open) {
        filename = getenv("GRIBTAB");
        if (filename == NULL) filename = getenv("gribtab");
        if (filename == NULL) filename = "gribtab";

        if ((input = fopen(filename,"r")) == NULL) {
            status = no_file;
            return 0;
        }
	file_open = 1;
    }
    else {
	rewind(input);
    }

    user_center = center;
    user_subcenter = subcenter;
    user_ptable = ptable;

    /* scan for center & subcenter and ptable */
    for (;;) {
        if (fgets(line, 299, input) == NULL) {
	    status = not_found;
            return 0;
        }
	if (atoi(line) != START) continue;
	i = sscanf(line,"%d:%d:%d:%d", &j, &center, &subcenter, &ptable);
        if (i != 4) {
	    fprintf(stderr,"illegal gribtab center/subcenter/ptable line: %s\n", line);
            continue;
        }
	if ((center == -1 || center == user_center) &&
	    (subcenter == -1 || subcenter == user_subcenter) &&
	    (ptable == -1 || ptable == user_ptable)) break;
    }

    user_center = center;
    user_subcenter = subcenter;
    user_ptable = ptable;

    /* free any used memory */
    for (i = 0; i < 256; i++) {
        if (parm_table_user[i].name != NULL) free(parm_table_user[i].name);
        if (parm_table_user[i].comment != NULL) free(parm_table_user[i].comment);
	parm_table_user[i].name = parm_table_user[i].comment = NULL;
    }

    /* read definitions */

    for (;;) {
        if (fgets(line, 299, input) == NULL) break;
	if ((i = atoi(line)) == START) break;
	line[299] = 0;

	/* find the colons and end-of-line */
	for (c0 = 0; line[c0] != ':' && line[c0] != 0; c0++) ;
        /* skip blank lines */
        if (line[c0] == 0) continue;

	for (c1 = c0 + 1; line[c1] != ':' && line[c1] != 0; c1++) ;
	c2 = strlen(line);
        if (line[c2-1] == '\n') line[--c2] = '\0';
        if (c2 <= c1) {
	    fprintf(stderr,"illegal gribtab line:%s\n", line);
	    continue;
	}
	line[c0] = 0;
	line[c1] = 0;

	parm_table_user[i].name = (char *) malloc(c1 - c0);
	parm_table_user[i].comment = (char *) malloc(c2 - c1);
	strcpy(parm_table_user[i].name, line+c0+1);
	strcpy(parm_table_user[i].comment, line+c1+1);
    }

    /* now to fill in undefined blanks */
    for (i = 0; i < 255; i++) {
	if (parm_table_user[i].name == NULL) {
	    parm_table_user[i].name = (char *) malloc(7);
	    sprintf(parm_table_user[i].name, "var%d", i);
	    parm_table_user[i].comment = (char *) malloc(strlen("undefined")+1);
	    strcpy(parm_table_user[i].comment, "undefined");
        }
    }
    status = filled;
    return 1;
}

/*
 * PDS_date.c  v1.2 wesley ebisuzaki
 *
 * prints a string with a date code
 *
 * PDS_date(pds,option, v_time)
 *   options=0  .. 2 digit year
 *   options=1  .. 4 digit year
 *
 *   v_time=0   .. initial time
 *   v_time=1   .. verification time
 *
 * assumption: P1 and P2 are unsigned integers (not clear from doc)
 *
 * v1.2 years that are multiple of 400 are leap years, not 500
 * v1.2.1  make the change to the source code for v1.2
 * v1.2.2  add 3/6/12 hour forecast time units
 */

static int msg_count = 0;
extern int minute;

int PDS_date(unsigned char *pds, int option, int v_time) {

    int year, month, day, hour, min;

    if (v_time == 0) {
        year = PDS_Year4(pds);
        month = PDS_Month(pds);
        day  = PDS_Day(pds);
        hour = PDS_Hour(pds);
    }
    else {
        if (verf_time(pds, &year, &month, &day, &hour) != 0) {
	    if (msg_count++ < 5) fprintf(stderr, "PDS_date: problem\n");
	}
    }
    min =  PDS_Minute(pds);

    switch(option) {
	case 0:
	    printf("%2.2d%2.2d%2.2d%2.2d", year % 100, month, day, hour);
	    if (minute) printf("-%2.2d", min);
	    break;
	case 1:
	    printf("%4.4d%2.2d%2.2d%2.2d", year, month, day, hour);
	    if (minute) printf("-%2.2d", min);
	    break;
	default:
	    fprintf(stderr,"missing code\n");
	    exit(8);
    }
    return 0;
}

#define  FEB29   (31+29)
static int monthjday[12] = {
        0,31,59,90,120,151,181,212,243,273,304,334};

static int leap(int year) {
	if (year % 4 != 0) return 0;
	if (year % 100 != 0) return 1;
	return (year % 400 == 0);
}


int add_time(int *year, int *month, int *day, int *hour, int dtime, int unit) {
    int y, m, d, h, jday, i;

    y = *year;
    m = *month;
    d = *day;
    h = *hour;

    if (unit == YEAR) {
	*year = y + dtime;
	return 0;
    }
    if (unit == DECADE) {
	*year =  y + (10 * dtime);
	return 0;
    }
    if (unit == CENTURY) {
	*year = y + (100 * dtime);
	return 0;
    }
    if (unit == NORMAL) {
	*year = y + (30 * dtime);
	return 0;
    }
    if (unit == MONTH) {
        if (dtime < 0) {
           i = (-dtime) / 12 + 1;
           y -= i;
           dtime += (i * 12);
        }
	dtime += (m - 1);
	*year = y + (dtime / 12);
	*month = 1 + (dtime % 12);
	return 0;
    }

    if (unit == SECOND) {
	dtime /= 60;
	unit = MINUTE;
    }
    if (unit == MINUTE) {
	dtime /= 60;
	unit = HOUR;
    }

    if (unit == HOURS3) {
        dtime *= 3;
        unit = HOUR;
    }
    else if (unit == HOURS6) {
        dtime *= 6;
        unit = HOUR;
    }
    else if (unit == HOURS12) {
        dtime *= 12;
        unit = HOUR;
    }

    if (unit == HOUR) {
	dtime += h;

        *hour = dtime % 24;
        dtime = dtime / 24;
        if (*hour < 0) {
            *hour += 24;
            dtime--;
        }
        unit = DAY;
    }

    /* this is the hard part */

    if (unit == DAY) {
	/* set m and day to Jan 0, and readjust dtime */
	jday = d + monthjday[m-1];
	if (leap(y) && m > 2) jday++;
        dtime += jday;

        while (dtime < 1) {
            y--;
	    dtime += 365 + leap(y);
        }

	/* one year chunks */
	while (dtime > 365 + leap(y)) {
	    dtime -= (365 + leap(y));
	    y++;
	}

	/* calculate the month and day */

	if (leap(y) && dtime == FEB29) {
	    m = 2;
	    d = 29;
	}
	else {
	    if (leap(y) && dtime > FEB29) dtime--;
	    for (i = 11; monthjday[i] >= dtime; --i);
	    m = i + 1;
	    d = dtime - monthjday[i];
	}
	*year = y;
	*month = m;
	*day = d;
	return 0;
   }
   fprintf(stderr,"add_time: undefined time unit %d\n", unit);
   return 1;
}


/*
 * verf_time:
 *
 * this routine returns the "verification" time
 * should have behavior similar to gribmap
 *
 */

int verf_time(unsigned char *pds, int *year, int *month, int *day, int *hour) {
    int tr, dtime, unit;

    *year = PDS_Year4(pds);
    *month = PDS_Month(pds);
    *day  = PDS_Day(pds);
    *hour = PDS_Hour(pds);

    /* find time increment */

    dtime = PDS_P1(pds);
    tr = PDS_TimeRange(pds);
    unit = PDS_ForecastTimeUnit(pds);

    if (tr == 10) dtime = PDS_P1(pds) * 256 + PDS_P2(pds);
    if (tr > 1 && tr < 6 ) dtime = PDS_P2(pds);
    if (tr == 6 || tr == 7) dtime = - PDS_P1(pds);

    if (dtime == 0) return 0;

    return add_time(year, month, day, hour, dtime, unit);
}


/*
 * ensemble.c   v0.1 wesley ebisuzaki
 *
 * prints ensemble meta-data
 *
 * only for NCEP and ECMWF
 *
 * output format:
 *
 *       ECMWF
 *  ens=n/N:       n:  0=ctl, +/-ve
 *                 N:  total number of members
 *
 *       NCEP
 *  ens=n/type:    n:  0=ctl, +/-ve, CLUST, PROD/
 *                 type: Mn, WtdMn, SDev, NSDev
 */

extern int ncep_ens;

void ensemble(unsigned char *pds, int mode) {

    int pdslen;
    unsigned char ctmp;
    char char_end;

    pdslen = PDS_LEN(pds);
    char_end = mode == 2 ? ' ' : ':';

    if ((PDS_Center(pds) == NMC || ncep_ens) && pdslen >= 45 && pds[40] == 1) {

	/* control run */

	if (pds[41] == 1) {
	    if (mode != 2) {
		printf("ens%c0:", pds[42] == 1 ? '+' : '-');
	    }
	    else {
		printf("%s-res_ens_control ", pds[42] == 1 ? "hi" : "low");
	    }
	}

	/* perturbation run */

	else if (pds[41] == 2 || pds[41] == 3) {
	    if (mode != 2) {
	        printf("ens%c%d:", pds[41] == 3 ? '+' : '-', pds[42]);
	    }
	    else {
		printf("ens_perturbation=%c%d ",pds[41] == 3 ? '+' : '-', 
		    pds[42]);
	    }
	}

	/* ensemble mean */

	else if (pds[41] == 5) {
	    /* makes no sense to say "ensemble mean" for prob forecasts */
            if (PDS_PARAM(pds) != 191 && PDS_PARAM(pds) != 192) {
	        if (mode != 2 || pdslen < 61) {
		    printf("ens-mean%c", char_end);
		}
		else {
		    printf("ensemble-mean(%d members) ",pds[60]);
		}
	    }
	}

	/* other case .. debug code */

	else {
		printf("ens %d/%d/%d/%d:", pds[41],pds[42],pds[43],pds[44]);
	}

	/* NCEP probability limits */

	if ((PDS_PARAM(pds) == 191 || PDS_PARAM(pds) == 192) && pdslen >= 47) {
	    ctmp = PDS_PARAM(pds);
	    PDS_PARAM(pds) = pds[45];
	    if (pds[46] == 1 && pdslen >= 51) {
		printf("prob(%s<%f)%c", k5toa(pds), ibm2flt(pds+47),char_end);
	    }
	    else if (pds[46] == 2 && pdslen >= 54) {
		printf("prob(%s>%f)%c", k5toa(pds), ibm2flt(pds+51), char_end);
	    }
	    else if (pds[46] == 3 && pdslen >= 54) {
		printf("prob(%f<%s<%f)%c", ibm2flt(pds+47), k5toa(pds), 
			ibm2flt(pds+51), char_end);
	    }
            PDS_PARAM(pds) = ctmp;
	}

    }
    /* ECMWF test should go here */
}

/*
 * GRIB table 2 at DWD
 *     Helmut P. Frank, 30.08.2001
 * updated 24.07.2003: PMSL, DD, FF, W, FR_ICE, H_ICE
 */

struct ParmTable parm_table_dwd_002[256] = {
    /* 0 */ {"var0", "undefined"},
    /* 1 */ {"PS", "pressure [Pa]"},
    /* 2 */ {"PMSL", "pressure reduced to MSL [Pa]"},
    /* 3 */ {"p-tendency", "pressure tendency [Pa/s]"},
    /* 4 */ {"var4", "undefined"},
    /* 5 */ {"var5", "undefined"},
    /* 6 */ {"FI", "geopotential [(m**2)/(s**2)]"},
    /* 7 */ {"geopot h", "geopotential height [gpm]"},
    /* 8 */ {"geomet h", "geometrical height [m]"},
    /* 9 */ {"dev of h", "standard deviation of height [m]"},
    /* 10 */ {"TO3", "total ozone [Dobson Units]"},
    /* 11 */ {"T", "temperature [K]"},
    /* 12 */ {"virt.temp.", "virtual temperature [K]"},
    /* 13 */ {"pot. temp.", "potential temperature [K]"},
    /* 14 */ {"pseudo-pot", "pseudo-adiabatic potential temperature [K]"},
    /* 15 */ {"TMAX", "maximum temperature [K]"},
    /* 16 */ {"TMIN", "minimum temperature [K]"},
    /* 17 */ {"TD", "dew-point temperature [K]"},
    /* 18 */ {"dew-pnt de", "dew-point depression (or deficit) [K]"},
    /* 19 */ {"lapse rate", "laps rate [K/m]"},
    /* 20 */ {"visibility", "visibility [m]"},
    /* 21 */ {"radar sp 1", "radar spectra (1) [non-dim]"},
    /* 22 */ {"radar sp 2", "radar spectra (2) [non-dim]"},
    /* 23 */ {"radar sp 3", "radar spectra (3) [non-dim]"},
    /* 24 */ {"pli to 500", "parcel lifted index (to 500 hPa) [K]"},
    /* 25 */ {"temp anom", "temperature anomaly [K]"},
    /* 26 */ {"pres anom", "pressure anomaly [Pa]"},
    /* 27 */ {"geop anom", "geopotential height anomaly [gpm]"},
    /* 28 */ {"wave sp 1", "wave spaectra(1) [non-dim]"},
    /* 29 */ {"wave sp 2", "wave spaectra(2) [non-dim]"},
    /* 30 */ {"wave sp 3", "wave spaectra(3) [non-dim]"},
    /* 31 */ {"DD", "wind direction [degree true]"},
    /* 32 */ {"FF", "wind speed [m/s]"},
    /* 33 */ {"U", "u-component (zonal) of wind [m/s]"},
    /* 34 */ {"V", "v-component (merdional) of wind [m/s]"},
    /* 35 */ {"stream fun", "stream function [(m**2)/s]"},
    /* 36 */ {"vel potent", "velocity potential [(m**2)/s]"},
    /* 37 */ {"M.stream f", "Montgomery stream function [(m**2)/(s**2)]"},
    /* 38 */ {"sigma vert", "sigma co-ordinate vertical velocity [1/s]"},
    /* 39 */ {"OMEGA", "vertical velocity [Pa/s]"},
    /* 40 */ {"W", "vertical velocity [m/s]"},
    /* 41 */ {"abs vortic", "absolute vorticity [1/s]"},
    /* 42 */ {"abs diverg", "absolute divergence [1/s]"},
    /* 43 */ {"rel vortic", "relative vorticity [1/s]"},
    /* 44 */ {"rel diverg", "relative divergence [1/s]"},
    /* 45 */ {"vert.u-shr", "vertical u-component shear [1/s]"},
    /* 46 */ {"vert.v-shr", "vertical v-component shear [1/s]"},
    /* 47 */ {"dir of cur", "direction of current [degree true]"},
    /* 48 */ {"spd of cur", "speed of current [m/s]"},
    /* 49 */ {"currcomp U", "u-component of current [m/s]"},
    /* 50 */ {"currcomp V", "v-component of current [m/s]"},
    /* 51 */ {"QV", "specific humidity [kg/kg]"},
    /* 52 */ {"RELHUM", "relative humidity [%]"},
    /* 53 */ {"hum mixrat", "humidity mixing ratio [kg/kg]"},
    /* 54 */ {"TQV", "total precipitable water [kg/m**2]"},
    /* 55 */ {"vapor pres", "vapor pressure [Pa]"},
    /* 56 */ {"sat.defic.", "saturation deficit [Pa]"},
    /* 57 */ {"evaporat.", "evaporation [kg/(m**2)]"},
    /* 58 */ {"TQI", "total cloud ice content [kg/m**2]"},
    /* 59 */ {"prec. rate", "precipitation rate [kg/((m**2)*s)]"},
    /* 60 */ {"thunderst.", "thunderstorm probability [%]"},
    /* 61 */ {"TOT_PREC", "total precipitation [kg/(m**2)]"},
    /* 62 */ {"ls precip.", "large scale precipitation [kg/(m**2)]"},
    /* 63 */ {"conv prec.", "convective precipitation [kg/(m**2)]"},
    /* 64 */ {"snowf.rate", "snowfall rate water equivalent [kg/((m**2)*s)]"},
    /* 65 */ {"W_SNOW", "water equivalent of accumulated snow depth [kg/(m**2)]"},
    /* 66 */ {"snow depth", "snow depth [m]"},
    /* 67 */ {"mix lay de", "mixed layer depth [m]"},
    /* 68 */ {"tr therm d", "transient thermocline depth [m]"},
    /* 69 */ {"ma therm d", "main thermocline depth [m]"},
    /* 70 */ {"m therm da", "main thermocline depth anomaly [m]"},
    /* 71 */ {"CLCT", "total cloud cover [%]"},
    /* 72 */ {"CLC_CON", "convective cloud cover [%]"},
    /* 73 */ {"CLCL", "low cloud cover [%]"},
    /* 74 */ {"CLCM", "medium cloud cover [%]"},
    /* 75 */ {"CLCH", "high cloud cover [%]"},
    /* 76 */ {"TQC", "total cloud water content [kg/m**2]"},
    /* 77 */ {"bli to 500", "best lifted index (to 500 hPa) [K]"},
    /* 78 */ {"SNOW_CON", "convective snow [kg/(m**2)]"},
    /* 79 */ {"SNOW_GSP", "large scale snow [kg/(m**2)]"},
    /* 80 */ {"water temp", "water temperature [K]"},
    /* 81 */ {"FR_LAND", "land cover (1=land, 0=sea) [1]"},
    /* 82 */ {"dev sea-le", "deviation of sea-level from mean [m]"},
    /* 83 */ {"Z0", "surface roughness [m]"},
    /* 84 */ {"ALB_RAD", "albedo [%]"},
    /* 85 */ {"T_soil", "soil temperature [K]"},
    /* 86 */ {"W_soil", "soil moisture content [kg/(m**2)]"},
    /* 87 */ {"PLCOV", "vegetation (plant cover) [%]"},
    /* 88 */ {"salinity", "salinity [kg/kg]"},
    /* 89 */ {"density", "density [kg/(m**3)]"},
    /* 90 */ {"RUNOFF", "water run-off [kg/(m**2)]"},
    /* 91 */ {"FR_ICE", "ice cover (1=ice, 0=no ice) [1]"},
    /* 92 */ {"H_ICE", "ice thickness [m]"},
    /* 93 */ {"dir ice dr", "direction of ice drift [degree true]"},
    /* 94 */ {"sp ice dr", "speed of ice drift [m/s]"},
    /* 95 */ {"ice dr u", "u-component of ice drift [m/s]"},
    /* 96 */ {"ice dr v", "v-component of ice drift [m/s]"},
    /* 97 */ {"ice growth", "ice growth rate [m/s]"},
    /* 98 */ {"ice diverg", "ice divergence [1/s]"},
    /* 99 */ {"snow melt", "snow melt [kg/(m**2)]"},
    /* 100 */ {"winwav/swe", "significant height of comb. wind waves and swell [m]"},
    /* 101 */ {"dir of wav", "direction of wind waves [degree true]"},
    /* 102 */ {"hei of wav", "significant height of wind waves [m]"},
    /* 103 */ {"MP of wiwa", "mean period of wind waves [s]"},
    /* 104 */ {"dir of swe", "direction of swell [degree true]"},
    /* 105 */ {"hei of swe", "significant height of swell [m]"},
    /* 106 */ {"MP of swel", "mean period of swell [s]"},
    /* 107 */ {"pr wave di", "primary wave direction [degree true]"},
    /* 108 */ {"pr wave pe", "primary wave period [s]"},
    /* 109 */ {"se wave di", "secondary wave direction [degree true]"},
    /* 110 */ {"se wave pe", "secondary wave period [s]"},
    /* 111 */ {"ASOB_S", "net short-wave radiation (surface) [W/(m**2)]"},
    /* 112 */ {"ATHB_S", "net long-wave radiation (surface) [W/(m**2)]"},
    /* 113 */ {"ASOB_T", "net short-wave radiation (top of atmosphere) [W/(m**2)]"},
    /* 114 */ {"ATHB_T", "net long-wave radiation (top of atmosphere) [W/(m**2)]"},
    /* 115 */ {"l-w rad.", "long-wave radiation [W/(m**2)]"},
    /* 116 */ {"s-w rad.", "short-wave radiation [W/(m**2)]"},
    /* 117 */ {"global rad", "global radiation [W/(m**2)]"},
    /* 118 */ {"var118", "undefined"},
    /* 119 */ {"var119", "undefined"},
    /* 120 */ {"var120", "undefined"},
    /* 121 */ {"ALHFL_S", "latent heat flux [W/(m**2)]"},
    /* 122 */ {"ASHFL_S", "sensible heat flux [W/(m**2)]"},
    /* 123 */ {"bound l di", "boundary layer dissipation [W/(m**2)]"},
    /* 124 */ {"AUMFL_S", "momentum flux, u component [N/(m**2)]"},
    /* 125 */ {"AVMFL_S", "momentum flux, v component [N/(m**2)]"},
    /* 126 */ {"wind mix e", "wind mixing energy [J]"},
    /* 127 */ {"image data", "image data []"},
    /* 128 */ {"var128", "undefined"},
    /* 129 */ {"geopot h", "geopotential height (ECMF) [gpm]"},
    /* 130 */ {"temperatur", "temperature (ECMF) [K]"},
    /* 131 */ {"wind compU", "u-component of wind (ECMF) [m/s]"},
    /* 132 */ {"wind compV", "v-component of wind (ECMF) [m/s]"},
    /* 133 */ {"var133", "undefined"},
    /* 134 */ {"var134", "undefined"},
    /* 135 */ {"var135", "undefined"},
    /* 136 */ {"var136", "undefined"},
    /* 137 */ {"var137", "undefined"},
    /* 138 */ {"var138", "undefined"},
    /* 139 */ {"soil temp.", "soil temperature (ECMF) [K]"},
    /* 140 */ {"var140", "undefined"},
    /* 141 */ {"var141", "undefined"},
    /* 142 */ {"ls precip.", "large scale precipitation (ECMF) [kg/(m**2)]"},
    /* 143 */ {"conv prec.", "convective precipitation (ECMF) [kg/(m**2)]"},
    /* 144 */ {"snowfall", "snowfall (ECMF) [m of water equivalent]"},
    /* 145 */ {"var145", "undefined"},
    /* 146 */ {"var146", "undefined"},
    /* 147 */ {"var147", "undefined"},
    /* 148 */ {"var148", "undefined"},
    /* 149 */ {"var149", "undefined"},
    /* 150 */ {"var150", "undefined"},
    /* 151 */ {"pressure", "pressure reduced to MSL (ECMF) [Pa]"},
    /* 152 */ {"var152", "undefined"},
    /* 153 */ {"var153", "undefined"},
    /* 154 */ {"var154", "undefined"},
    /* 155 */ {"var155", "undefined"},
    /* 156 */ {"geopot h", "geopotential height (ECMF) [gpm]"},
    /* 157 */ {"rel. humid", "relative humidity (ECMF) [%]"},
    /* 158 */ {"var158", "undefined"},
    /* 159 */ {"var159", "undefined"},
    /* 160 */ {"var160", "undefined"},
    /* 161 */ {"var161", "undefined"},
    /* 162 */ {"var162", "undefined"},
    /* 163 */ {"var163", "undefined"},
    /* 164 */ {"cloud cov.", "total cloud cover (ECMF) [%]"},
    /* 165 */ {"10m-wind U", "u-component of 10m-wind (ECMF) [m/s]"},
    /* 166 */ {"10m-wind V", "v-component of 10m-wind (ECMF) [m/s]"},
    /* 167 */ {"2m temper", "2m temperature (ECMF) [K]"},
    /* 168 */ {"2m due-p.", "2m due-point temperature (ECMF) [K]"},
    /* 169 */ {"var169", "undefined"},
    /* 170 */ {"var170", "undefined"},
    /* 171 */ {"var171", "undefined"},
    /* 172 */ {"var172", "undefined"},
    /* 173 */ {"var173", "undefined"},
    /* 174 */ {"var174", "undefined"},
    /* 175 */ {"var175", "undefined"},
    /* 176 */ {"var176", "undefined"},
    /* 177 */ {"var177", "undefined"},
    /* 178 */ {"var178", "undefined"},
    /* 179 */ {"var179", "undefined"},
    /* 180 */ {"var180", "undefined"},
    /* 181 */ {"var181", "undefined"},
    /* 182 */ {"var182", "undefined"},
    /* 183 */ {"var183", "undefined"},
    /* 184 */ {"var184", "undefined"},
    /* 185 */ {"var185", "undefined"},
    /* 186 */ {"var186", "undefined"},
    /* 187 */ {"var187", "undefined"},
    /* 188 */ {"var188", "undefined"},
    /* 189 */ {"var189", "undefined"},
    /* 190 */ {"var190", "undefined"},
    /* 191 */ {"var191", "undefined"},
    /* 192 */ {"var192", "undefined"},
    /* 193 */ {"var193", "undefined"},
    /* 194 */ {"var194", "undefined"},
    /* 195 */ {"var195", "undefined"},
    /* 196 */ {"var196", "undefined"},
    /* 197 */ {"var197", "undefined"},
    /* 198 */ {"var198", "undefined"},
    /* 199 */ {"var199", "undefined"},
    /* 200 */ {"var200", "undefined"},
    /* 201 */ {"var201", "undefined"},
    /* 202 */ {"var202", "undefined"},
    /* 203 */ {"var203", "undefined"},
    /* 204 */ {"var204", "undefined"},
    /* 205 */ {"var205", "undefined"},
    /* 206 */ {"var206", "undefined"},
    /* 207 */ {"var207", "undefined"},
    /* 208 */ {"var208", "undefined"},
    /* 209 */ {"var209", "undefined"},
    /* 210 */ {"var210", "undefined"},
    /* 211 */ {"var211", "undefined"},
    /* 212 */ {"var212", "undefined"},
    /* 213 */ {"var213", "undefined"},
    /* 214 */ {"var214", "undefined"},
    /* 215 */ {"var215", "undefined"},
    /* 216 */ {"var216", "undefined"},
    /* 217 */ {"var217", "undefined"},
    /* 218 */ {"var218", "undefined"},
    /* 219 */ {"var219", "undefined"},
    /* 220 */ {"var220", "undefined"},
    /* 221 */ {"var221", "undefined"},
    /* 222 */ {"var222", "undefined"},
    /* 223 */ {"var223", "undefined"},
    /* 224 */ {"var224", "undefined"},
    /* 225 */ {"var225", "undefined"},
    /* 226 */ {"var226", "undefined"},
    /* 227 */ {"var227", "undefined"},
    /* 228 */ {"total prec", "total precipitation (ECMF) [m]"},
    /* 229 */ {"seaway 01", "seaway 01 (ECMF) []"},
    /* 230 */ {"seaway 02", "seaway 02 (ECMF) []"},
    /* 231 */ {"seaway 03", "seaway 03 (ECMF) []"},
    /* 232 */ {"seaway 04", "seaway 04 (ECMF) []"},
    /* 233 */ {"seaway 05", "seaway 05 (ECMF) []"},
    /* 234 */ {"seaway 06", "seaway 06 (ECMF) []"},
    /* 235 */ {"seaway 07", "seaway 07 (ECMF) []"},
    /* 236 */ {"seaway 08", "seaway 08 (ECMF) []"},
    /* 237 */ {"seaway 09", "seaway 09 (ECMF) []"},
    /* 238 */ {"seaway 10", "seaway 10 (ECMF) []"},
    /* 239 */ {"seaway 11", "seaway 11 (ECMF) []"},
    /* 240 */ {"var240", "undefined"},
    /* 241 */ {"var241", "undefined"},
    /* 242 */ {"var242", "undefined"},
    /* 243 */ {"var243", "undefined"},
    /* 244 */ {"var244", "undefined"},
    /* 245 */ {"var245", "undefined"},
    /* 246 */ {"var246", "undefined"},
    /* 247 */ {"var247", "undefined"},
    /* 248 */ {"var248", "undefined"},
    /* 249 */ {"var249", "undefined"},
    /* 250 */ {"var250", "undefined"},
    /* 251 */ {"var251", "undefined"},
    /* 252 */ {"var252", "undefined"},
    /* 253 */ {"var253", "undefined"},
    /* 254 */ {"var254", "undefined"},
    /* 255 */ {"var255", "undefined"},
};

/*
 * GRIB table 201 at DWD
 *     Helmut P. Frank, 30.08.2001
 * updated 24.07.2003:  DQC_GSP, DQI_GSP, T_SO, W_SO, W_SO_ICE, T_ICE
 */

struct ParmTable parm_table_dwd_201[256] = {
    /* 0 */ {"var0", "undefined"},
    /* 1 */ {"dw sw flux", "downward shortwave radiant flux density [W/m**2]"},
    /* 2 */ {"uw sw flux", "upward shortwave radiant flux density [W/m**2]"},
    /* 3 */ {"dw lw flux", "downward longwave radiant flux density [W/m**2]"},
    /* 4 */ {"uw lw flux", "upward longwave radiant flux density [W/m**2]"},
    /* 5 */ {"APAB_S", "downwd photosynthetic active radiant flux density [W/m**2]"},
    /* 6 */ {"net s flux", "net shortwave flux [W/m**2]"},
    /* 7 */ {"net l flux", "net longwave flux [W/m**2]"},
    /* 8 */ {"net flux", "total net radiative flux density [W/m**2]"},
    /* 9 */ {"dw sw clfr", "downw shortw radiant flux density, cloudfree part [W/m**2]"},
    /* 10 */ {"uw sw cldy", "upw shortw radiant flux density, cloudy part [W/m**2]"},
    /* 11 */ {"dw lw clfr", "downw longw radiant flux density, cloudfree part [W/m**2]"},
    /* 12 */ {"uw lw cldy", "upw longw radiant flux density, cloudy part [W/m**2]"},
    /* 13 */ {"SOHR_RAD", "shortwave radiative heating rate [K/s]"},
    /* 14 */ {"THHR_RAD", "longwave radiative heating rate [K/s]"},
    /* 15 */ {"rad heat", "total radiative heating rate [K/s]"},
    /* 16 */ {"soilheat S", "soil heat flux, surface [W/m**2]"},
    /* 17 */ {"soilheat L", "soil heat flux, bottom of layer [W/m**2]"},
    /* 18 */ {"var18", "undefined"},
    /* 19 */ {"var19", "undefined"},
    /* 20 */ {"var20", "undefined"},
    /* 21 */ {"var21", "undefined"},
    /* 22 */ {"var22", "undefined"},
    /* 23 */ {"var23", "undefined"},
    /* 24 */ {"var24", "undefined"},
    /* 25 */ {"var25", "undefined"},
    /* 26 */ {"var26", "undefined"},
    /* 27 */ {"var27", "undefined"},
    /* 28 */ {"var28", "undefined"},
    /* 29 */ {"CLC", "cloud cover, grid scale + convective [1]"},
    /* 30 */ {"clc gr sc", "cloud cover, grid scale  (0...1) [1]"},
    /* 31 */ {"QC", "specific cloud water content, grid scale [kg/kg]"},
    /* 32 */ {"clw gs vi", "cloud water content, grid scale, vert integrated [kg/m**2]"},
    /* 33 */ {"QI", "specific cloud ice   content, grid scale [kg/kg]"},
    /* 34 */ {"cli gs vi", "cloud ice content, grid scale, vert integrated [kg/m**2]"},
    /* 35 */ {"src gr sc", "specific rainwater content, grid scale [kg/kg]"},
    /* 36 */ {"ssc gr sc", "specific snow content, grid scale [kg/kg]"},
    /* 37 */ {"src gs vi", "specific rainwater content, gs, vert. integrated [kg/m**2]"},
    /* 38 */ {"ssc gs vi", "specific snow content, gs, vert. integrated [kg/m**2]"},
    /* 39 */ {"var39", "undefined"},
    /* 40 */ {"var40", "undefined"},
    /* 41 */ {"tot water", "vert. integral of humidity, cloud water (and ice) [kg/(m**2)]"},
    /* 42 */ {"hum div", "vert. integral of divergence of tot. water content [kg/(m**2)]"},
    /* 43 */ {"var43", "undefined"},
    /* 44 */ {"var44", "undefined"},
    /* 45 */ {"var45", "undefined"},
    /* 46 */ {"var46", "undefined"},
    /* 47 */ {"var47", "undefined"},
    /* 48 */ {"var48", "undefined"},
    /* 49 */ {"var49", "undefined"},
    /* 50 */ {"CH_CM_CL", "cloud covers CH_CM_CL (000...888) [1]"},
    /* 51 */ {"cl cov. CH", "cloud cover CH (0..8) [1]"},
    /* 52 */ {"cl cov. CM", "cloud cover CM (0..8) [1]"},
    /* 53 */ {"cl cov. CL", "cloud cover CL (0..8) [1]"},
    /* 54 */ {"cloud cov.", "total cloud cover (0..8) [1]"},
    /* 55 */ {"fog", "fog (0..8) [1]"},
    /* 56 */ {"fog", "fog [1]"},
    /* 57 */ {"var57", "undefined"},
    /* 58 */ {"var58", "undefined"},
    /* 59 */ {"var59", "undefined"},
    /* 60 */ {"clc con ci", "cloud cover, convective cirrus  (0...1) [1]"},
    /* 61 */ {"clw con", "specific cloud water content, convective clouds [kg/kg]"},
    /* 62 */ {"clw con vi", "cloud water content, conv clouds, vert integrated [kg/m**2]"},
    /* 63 */ {"cli con", "specific cloud ice content, convective clouds [kg/kg]"},
    /* 64 */ {"cli con vi", "cloud ice content, conv clouds, vert integrated [kg/m**2]"},
    /* 65 */ {"mass fl co", "convective mass flux [kg/(s*m**2)]"},
    /* 66 */ {"upd vel co", "updraft velocity, convection [m/s]"},
    /* 67 */ {"entr p co", "entrainment parameter, convection [m**(-1)]"},
    /* 68 */ {"HBAS_CON", "cloud base, convective clouds (above msl) [m]"},
    /* 69 */ {"HTOP_CON", "cloud top, convective clouds (above msl) [m]"},
    /* 70 */ {"con layers", "convective layers (00...77)  (BKE) [1]"},
    /* 71 */ {"KO-index", "KO-index [1]"},
    /* 72 */ {"BAS_CON", "convection base index [1]"},
    /* 73 */ {"TOP_CON", "convection top index [1]"},
    /* 74 */ {"DT_CON", "convective temperature tendency [K/s]"},
    /* 75 */ {"DQV_CON", "convective tendency of specific humidity [s**(-1)]"},
    /* 76 */ {"H ten co", "convective tendency of total heat [J/(kg*s)]"},
    /* 77 */ {"QDW ten co", "convective tendency of total water [s**(-1)]"},
    /* 78 */ {"DU_CON", "convective momentum tendency (X-component) [m/s**2]"},
    /* 79 */ {"DV_CON", "convective momentum tendency (Y-component) [m/s**2]"},
    /* 80 */ {"vor ten co", "convective vorticity tendency [s**(-2)]"},
    /* 81 */ {"div ten co", "convective divergence tendency [s**(-2)]"},
    /* 82 */ {"HTOP_DC", "top of dry convection (above msl) [m]"},
    /* 83 */ {"top ind dc", "dry convection top index [1]"},
    /* 84 */ {"HZEROCL", "height of 0 degree Celsius isotherm above msl [m]"},
    /* 85 */ {"var85", "undefined"},
    /* 86 */ {"var86", "undefined"},
    /* 87 */ {"var87", "undefined"},
    /* 88 */ {"var88", "undefined"},
    /* 89 */ {"var89", "undefined"},
    /* 90 */ {"var90", "undefined"},
    /* 91 */ {"var91", "undefined"},
    /* 92 */ {"var92", "undefined"},
    /* 93 */ {"var93", "undefined"},
    /* 94 */ {"var94", "undefined"},
    /* 95 */ {"var95", "undefined"},
    /* 96 */ {"var96", "undefined"},
    /* 97 */ {"var97", "undefined"},
    /* 98 */ {"var98", "undefined"},
    /* 99 */ {"QRS_GSP", "spec. content of precip. particles [kg/kg]"},
    /* 100 */ {"PRR_GSP", "surface precipitation rate, rain, grid scale [kg/(s*m**2)]"},
    /* 101 */ {"PRS_GSP", "surface precipitation rate, snow, grid scale [kg/(s*m**2)]"},
    /* 102 */ {"RAIN_GSP", "surface precipitation amount, rain, grid scale [kg/m**2]"},
    /* 103 */ {"condens gs", "condensation rate, grid scale [kg/(kg*s)]"},
    /* 104 */ {"autocon gs", "autoconversion rate, grid scale   (C+C  --> R) [kg/(kg*s)]"},
    /* 105 */ {"accret gs", "accretion rate, grid scale        (R+C  --> R) [kg/(kg*s)]"},
    /* 106 */ {"nucleat gs", "nucleation rate, grid scale       (C+C  --> S) [kg/(kg*s)]"},
    /* 107 */ {"riming gs", "riming rate, grid scale           (S+C  --> S) [kg/(kg*s)]"},
    /* 108 */ {"deposit gs", "deposition rate, grid scale       (S+V <--> S) [kg/(kg*s)]"},
    /* 109 */ {"melting gs", "melting rate, grid scale          (S    --> R) [kg/(kg*s)]"},
    /* 110 */ {"evapor gs", "evaporation rate, grid scale      (R+V <--  R) [kg/(kg*s)]"},
    /* 111 */ {"PRR_CON", "surface precipitation rate, rain, convective [kg/(s*m**2)]"},
    /* 112 */ {"PRS_CON", "surface precipitation rate, snow, convective [kg/(s*m**2)]"},
    /* 113 */ {"RAIN_CON", "surface precipitation amount, rain, convective [kg/m**2]"},
    /* 114 */ {"condens co", "condensation rate, convective [kg/(kg*s)]"},
    /* 115 */ {"autocon co", "autoconversion rate, convective [kg/(kg*s)]"},
    /* 116 */ {"accret co", "accretion rate, convective [kg/(kg*s)]"},
    /* 117 */ {"nucleat co", "nucleation rate, convective [kg/(kg*s)]"},
    /* 118 */ {"riming co", "riming rate, convective [kg/(kg*s)]"},
    /* 119 */ {"sublim co", "sublimation rate, convective [kg/(kg*s)]"},
    /* 120 */ {"melting co", "melting rate, convective [kg/(kg*s)]"},
    /* 121 */ {"evapor co", "evaporation rate, convective [kg/(kg*s)]"},
    /* 122 */ {"rain am", "rain amount, grid-scale plus convective [kg/m**2]"},
    /* 123 */ {"snow am", "snow amount, grid-scale plus convective [kg/m**2]"},
    /* 124 */ {"DT_GSP", "temperature tendency, grid-scale condensation [K/s]"},
    /* 125 */ {"DQV_GSP", "tendency of specific humidity, grid-scale precip. [s**(-1)]"},
    /* 126 */ {"H ten gs", "tendency of total heat, grid-scale condensation [J/(kg*s)]"},
    /* 127 */ {"DQC_GSP", "tendency of spec. clod liquid water due to grid-scale precip. [s**(-1)]"},
    /* 128 */ {"snowfall", "snowfall  (dimension"},
    /* 129 */ {"DQI_GSP", "tendency of spec. cloud ice due to grid-scale precip. [s**(-1)]"},
    /* 130 */ {"var130", "undefined"},
    /* 131 */ {"var131", "undefined"},
    /* 132 */ {"var132", "undefined"},
    /* 133 */ {"var133", "undefined"},
    /* 134 */ {"var134", "undefined"},
    /* 135 */ {"var135", "undefined"},
    /* 136 */ {"var136", "undefined"},
    /* 137 */ {"var137", "undefined"},
    /* 138 */ {"var138", "undefined"},
    /* 139 */ {"pprime", "deviation of pressure from reference value [Pa]"},
    /* 140 */ {"var140", "undefined"},
    /* 141 */ {"var141", "undefined"},
    /* 142 */ {"var142", "undefined"},
    /* 143 */ {"var143", "undefined"},
    /* 144 */ {"var144", "undefined"},
    /* 145 */ {"var145", "undefined"},
    /* 146 */ {"var146", "undefined"},
    /* 147 */ {"var147", "undefined"},
    /* 148 */ {"var148", "undefined"},
    /* 149 */ {"var149", "undefined"},
    /* 150 */ {"hdi coeff", "coefficient of horizontal diffusion [m**2/s]"},
    /* 151 */ {"dissp rate", "dissipation rate [W/(Pa*m**2)]"},
    /* 152 */ {"TKE", "turbulent kinetic energy [(m/s)**2]"},
    /* 153 */ {"TKVM", "coefficient of vertical diffusion, momentum [m**2/s]"},
    /* 154 */ {"TKVH", "coefficient of vertical diffusion, heat [m**2/s]"},
    /* 155 */ {"vdi coe cw", "coefficient of vertical diffusion, cloud water [m**2/s]"},
    /* 156 */ {"vdi coe ci", "coefficient of vertical diffusion, cloud ice [m**2/s]"},
    /* 157 */ {"vdi coe vp", "coefficient of vertical diffusion, water vapour [m**2/s]"},
    /* 158 */ {"dis len m", "turbulent dissipation length for momentum [m]"},
    /* 159 */ {"dis len h", "turbulent dissipation length for heat [m]"},
    /* 160 */ {"var u mom", "variance of u-component of momentum [(m/s)**2]"},
    /* 161 */ {"var v mom", "variance of v-component of momentum [(m/s)**2]"},
    /* 162 */ {"var w mom", "variance of w-component of momentum [(m/s)**2]"},
    /* 163 */ {"var temp", "variance of temperature [K**2]"},
    /* 164 */ {"var cl wat", "variance of specific cloud water content [(kg/kg)**2]"},
    /* 165 */ {"var cl ice", "variance of specific cloud ice content [(kg/kg)**2]"},
    /* 166 */ {"var vap mr", "variance of water vapour mixing ratio [(kg/kg)**2]"},
    /* 167 */ {"c wat flux", "turbulent vertical flux of spec cloud water [m/s]"},
    /* 168 */ {"c ice flux", "turbulent vertical flux of spec cloud ice [m/s]"},
    /* 169 */ {"w vap flux", "turbulent vertical flux of water vapour mix ratio [m/s]"},
    /* 170 */ {"TCM", "drag coefficient CD [1]"},
    /* 171 */ {"TCH", "transfer coefficient CH (sensible heat) [1]"},
    /* 172 */ {"tr coef CQ", "transfer coefficient CQ (latent heat) [1]"},
    /* 173 */ {"PBL-top h", "PBL-top h [m]"},
    /* 174 */ {"T-jump  h", "temperature jump at PBL-top [K]"},
    /* 175 */ {"q-jump  h", "specific humidity jump at PBL-top [kg/kg]"},
    /* 176 */ {"entr at h", "entrainment at PBL-top [kg/(s*m**2)]"},
    /* 177 */ {"mass fl h", "upward mass flux at PBL-top [kg/(s*m**2)]"},
    /* 178 */ {"cl cov PBL", "cloud cover of PBL-clouds (0...1) [1]"},
    /* 179 */ {"cl wat PBL", "specific cloud water content of PBL-clouds [kg/kg]"},
    /* 180 */ {"cl top PBL", "cloud top of PBL-clouds [m]"},
    /* 181 */ {"cl bas PBL", "cloud base of PBL-clouds [m]"},
    /* 182 */ {"moun wav X", "vertical mountain wave momentum flux (X component) [kg/(m*s**2)]"},
    /* 183 */ {"moun wav Y", "vertical mountain wave momentum flux (Y component) [kg/(m*s**2)]"},
    /* 184 */ {"wave Ri", "wave Richardson number [1]"},
    /* 185 */ {"wav div X", "mountain wave momentum flux divergence (X comp) [m/s**2]"},
    /* 186 */ {"wav div Y", "mountain wave momentum flux divergence (Y comp) [m/s**2]"},
    /* 187 */ {"VMAX_10M", "maximum wind velocity [m/s]"},
    /* 188 */ {"wav dis vi", "mountain wave dissipation, vert integrated [W/m**2]"},
    /* 189 */ {"wv en flux", "vertical wave energy flux [kg*m/s**4]"},
    /* 190 */ {"var190", "undefined"},
    /* 191 */ {"var191", "undefined"},
    /* 192 */ {"var192", "undefined"},
    /* 193 */ {"var193", "undefined"},
    /* 194 */ {"var194", "undefined"},
    /* 195 */ {"var195", "undefined"},
    /* 196 */ {"var196", "undefined"},
    /* 197 */ {"T_SO", "soil temperature [K]"},
    /* 198 */ {"W_SO", "soil water content [kg/m**2]"},
    /* 199 */ {"W_SO_ICE", "soil ice water content [kg/m**2]"},
    /* 200 */ {"W_I", "water content of interception store [kg/(m**2)]"},
    /* 201 */ {"interc ice", "icebit for interception store [1]"},
    /* 202 */ {"snow fract", "snow fraction [1]"},
    /* 203 */ {"T_SNOW", "snow temperature [K]"},
    /* 204 */ {"foliag tem", "foliage temperature [K]"},
    /* 205 */ {"infiltrat", "infiltration [m/s]"},
    /* 206 */ {"runoff", "runoff [m/s]"},
    /* 207 */ {"soil evap", "bare soil evaporation [m/s]"},
    /* 208 */ {"plant tran", "plant transpiration [m/s]"},
    /* 209 */ {"inter evap", "interception store evaporation [m/s]"},
    /* 210 */ {"water evap", "evaporation from water surfaces [m/s]"},
    /* 211 */ {"aero resis", "aerodynamic resistance [s/m]"},
    /* 212 */ {"plant res", "plant resistance [s/m]"},
    /* 213 */ {"soil res", "soil resistance [s/m]"},
    /* 214 */ {"total evap", "total evaporation (water, soil, plants) [m/s]"},
    /* 215 */ {"T_ICE", "ice surface temperature [K]"},
    /* 216 */ {"var216", "undefined"},
    /* 217 */ {"var217", "undefined"},
    /* 218 */ {"var218", "undefined"},
    /* 219 */ {"var219", "undefined"},
    /* 220 */ {"var220", "undefined"},
    /* 221 */ {"var221", "undefined"},
    /* 222 */ {"var222", "undefined"},
    /* 223 */ {"var223", "undefined"},
    /* 224 */ {"var224", "undefined"},
    /* 225 */ {"var225", "undefined"},
    /* 226 */ {"var226", "undefined"},
    /* 227 */ {"var227", "undefined"},
    /* 228 */ {"var228", "undefined"},
    /* 229 */ {"var229", "undefined"},
    /* 230 */ {"XYZ", "S1 [1]"},
    /* 231 */ {"S2", "S2 [1]"},
    /* 232 */ {"S3", "S3 [1]"},
    /* 233 */ {"S4", "S4 [1]"},
    /* 234 */ {"S5", "S5 [1]"},
    /* 235 */ {"S6", "S6 [1]"},
    /* 236 */ {"S7", "S7 [1]"},
    /* 237 */ {"S8", "S8 [1]"},
    /* 238 */ {"S9", "S9 [1]"},
    /* 239 */ {"S10", "S10 [1]"},
    /* 240 */ {"S11", "S11 [1]"},
    /* 241 */ {"OBS TS oc", "OBS Gewitter (occasional) [1]"},
    /* 242 */ {"OBS TS fq", "OBS Gewitter (frequent) [1]"},
    /* 243 */ {"MOS pTS oc", "MOS Gewitter-Wahrscheinlichkeit (occasional) [1]"},
    /* 244 */ {"MOS pTS fq", "MOS Gewitter-Wahrscheinlichkeit (frequent) [1]"},
    /* 245 */ {"MOS TS cov", "MOS Gewitteranteil (occasional - frequent (1 - 2)) [1]"},
    /* 246 */ {"S17", "S17 [1]"},
    /* 247 */ {"S18", "S18 [1]"},
    /* 248 */ {"S19", "S19 [1]"},
    /* 249 */ {"S20", "S20 [1]"},
    /* 250 */ {"var250", "undefined"},
    /* 251 */ {"var251", "undefined"},
    /* 252 */ {"var252", "undefined"},
    /* 253 */ {"var253", "undefined"},
    /* 254 */ {"var254", "undefined"},
    /* 255 */ {"var255", "undefined"},
};

/*
 * GRIB table 202 at DWD
 *     Helmut P. Frank, 30.08.2001
 * updated 24.07.2003: UV_Ind_F_h, BasicUV_IF, UV_Ind_W_h, UV_IndmaxF,
 *                     "gesamt O3", UV_IndmaxW, "h UV_IndMx"
 */

struct ParmTable parm_table_dwd_202[256] = {
    /* 0 */ {"var0", "undefined"},
    /* 1 */ {"Seeg_peak", "jonswap parameter fm [s**(-1)]"},
    /* 2 */ {"Seeg_alpha", "jonswap parameter alpha [1]"},
    /* 3 */ {"Seeg_gamma", "jonswap parameter gamma [1]"},
    /* 4 */ {"Seeg_dir", "Seegang direction [degree true]"},
    /* 5 */ {"Seeg_energ", "Seegang energy densitiy [(m**2)*(s**2)]"},
    /* 6 */ {"Seeg_icemk", "Seegang ice mask [1]"},
    /* 7 */ {"peak p sw", "peak period of swell [s]"},
    /* 8 */ {"peak p ww", "peak period of wind waves [s]"},
    /* 9 */ {"var9", "undefined"},
    /* 10 */ {"var10", "undefined"},
    /* 11 */ {"var11", "undefined"},
    /* 12 */ {"var12", "undefined"},
    /* 13 */ {"var13", "undefined"},
    /* 14 */ {"var14", "undefined"},
    /* 15 */ {"var15", "undefined"},
    /* 16 */ {"var16", "undefined"},
    /* 17 */ {"var17", "undefined"},
    /* 18 */ {"var18", "undefined"},
    /* 19 */ {"var19", "undefined"},
    /* 20 */ {"Var. Geop.", "Varianz Geopotential [(m/s)**4]"},
    /* 21 */ {"Var. T", "Varianz Temperatur [K**2]"},
    /* 22 */ {"Var. u", "Varianz Zonalwind [(m/s)**2]"},
    /* 23 */ {"Var. v", "Varianz Meridionalwind [(m/s)**2]"},
    /* 24 */ {"Var. q", "Varianz spezifische Feuchte [(kg/kg)**2]"},
    /* 25 */ {"Mer. Imptr", "Meridionaler Impulstransport [(m/s)**2]"},
    /* 26 */ {"Mer. TrEpt", "Meridionaler Transport potentieller Energie [(m/s)**3]"},
    /* 27 */ {"Mer. TrsW", "Meridionaler Transport sensibler Waerme [K*(m/s)]"},
    /* 28 */ {"Mer. TrlW", "Meridionaler Transport latenter Waerme [(kg/kg)*(m/s)]"},
    /* 29 */ {"Ver. TrEpt", "Vertikaler Transport potentieller Energie [(m/s)**2*(Pa/s)]"},
    /* 30 */ {"Ver. TrsW", "Vertikaler Transport sensibler Waerme [K*(Pa/s)]"},
    /* 31 */ {"Ver.TrlW", "Vertikaler Transport latenter Waerme [(kg/kg)*(Pa/s)]"},
    /* 32 */ {"var32", "undefined"},
    /* 33 */ {"var33", "undefined"},
    /* 34 */ {"var34", "undefined"},
    /* 35 */ {"var35", "undefined"},
    /* 36 */ {"var36", "undefined"},
    /* 37 */ {"var37", "undefined"},
    /* 38 */ {"var38", "undefined"},
    /* 39 */ {"var39", "undefined"},
    /* 40 */ {"VarAF Geop", "Varianz des Analyse-Fehlers Geopotential [(m/s)**4]"},
    /* 41 */ {"VarAF u", "Varianz des Analyse-Fehlers Zonalwind [(m/s)**2]"},
    /* 42 */ {"VarAF v", "Varianz des Analyse-Fehlers Meridionalwind [(m/s)**2]"},
    /* 43 */ {"var43", "undefined"},
    /* 44 */ {"DU_SSO", "undefined"},
    /* 45 */ {"DV_SSO", "undefined"},
    /* 46 */ {"SSO_STDH", "standard deviation of subgrid scale orogr. height [m]"},
    /* 47 */ {"SSO_GAMMA", "anisotropy of topography [1]"},
    /* 48 */ {"SSO_THETA", "angle betw. principal axis of orogr. and global E [1]"},
    /* 49 */ {"SSO_SIGMA", "mean slope of subgrid scale orography [1]"},
    /* 50 */ {"oro varian", "subgrid-scale variance of orography [m**2]"},
    /* 51 */ {"E-W oro va", "E-W component of subgrid-scale variance of orogr [m**2]"},
    /* 52 */ {"N-S oro va", "N-S component of subgrid-scale variance of orogr [m**2]"},
    /* 53 */ {"NW-SE o va", "NW-SE component of subgrid-scale variance of orogr [m**2]"},
    /* 54 */ {"NE-SW o va", "NE-SW component of subgrid-scale variance of orogr [m**2]"},
    /* 55 */ {"inl w frac", "fraction of inland water [1]"},
    /* 56 */ {"surf emiss", "surface emissivity [1]"},
    /* 57 */ {"SOILTYP", "soil texture [1]"},
    /* 58 */ {"soil color", "soil color [1]"},
    /* 59 */ {"soil drain", "soil drainage [1]"},
    /* 60 */ {"ground wat", "ground water table [m]"},
    /* 61 */ {"LAI", "leaf area index [1]"},
    /* 62 */ {"ROOT", "root depth [m]"},
    /* 63 */ {"root dens", "root density [1]"},
    /* 64 */ {"HMO3", "height of maximum of ozone concentration [Pa]"},
    /* 65 */ {"VIO3", "total vertically integrated ozone content [Pa]"},
    /* 66 */ {"ld-sea msk", "land-sea mask [1]"},
    /* 67 */ {"PLCOV_MX", "ground fraction covered by plants (vegetation p.) [1]"},
    /* 68 */ {"PLCOV_MN", "ground fraction covered by plants (time of rest) [1]"},
    /* 69 */ {"LAI_MX", "leaf area index (vegetation period) [1]"},
    /* 70 */ {"LAI_MN", "leaf area index (time of rest) [1]"},
    /* 71 */ {"Orographie", "Orographie + Land-Meer-Verteilung [m]"},
    /* 72 */ {"r length m", "roughness length momentum [m]"},
    /* 73 */ {"r length h", "roughness length heat [m]"},
    /* 74 */ {"var smc", "variance of soil moisture content [kg**2/m**4]"},
    /* 75 */ {"FOR_E", "fractional coverage with evergreen forest [1]"},
    /* 76 */ {"FOR_D", "fractional coverage with deciduous forest [1]"},
    /* 77 */ {"var77", "undefined"},
    /* 78 */ {"var78", "undefined"},
    /* 79 */ {"var79", "undefined"},
    /* 80 */ {"var80", "undefined"},
    /* 81 */ {"var81", "undefined"},
    /* 82 */ {"var82", "undefined"},
    /* 83 */ {"var83", "undefined"},
    /* 84 */ {"var84", "undefined"},
    /* 85 */ {"var85", "undefined"},
    /* 86 */ {"var86", "undefined"},
    /* 87 */ {"var87", "undefined"},
    /* 88 */ {"var88", "undefined"},
    /* 89 */ {"var89", "undefined"},
    /* 90 */ {"var90", "undefined"},
    /* 91 */ {"var91", "undefined"},
    /* 92 */ {"var92", "undefined"},
    /* 93 */ {"var93", "undefined"},
    /* 94 */ {"var94", "undefined"},
    /* 95 */ {"var95", "undefined"},
    /* 96 */ {"var96", "undefined"},
    /* 97 */ {"var97", "undefined"},
    /* 98 */ {"var98", "undefined"},
    /* 99 */ {"AER_DES", "undefined"},
    /* 100 */ {"var100", "undefined"},
    /* 101 */ {"tidal tend", "tidal tendencies [(m/s)**2]"},
    /* 102 */ {"diab heatg", "sum of diabatic heating terms [K/s]"},
    /* 103 */ {"adiab heat", "total adiabatic heating [K/s]"},
    /* 104 */ {"adv q tend", "advective tendency of specific humidity [s**(-1)]"},
    /* 105 */ {"nadv q ten", "non-advective tendency of specific humidity [s**(-1)]"},
    /* 106 */ {"adv m te X", "advective momentum tendency (X component) [m/s**2]"},
    /* 107 */ {"adv m te Y", "advective momentum tendency (Y component) [m/s**2]"},
    /* 108 */ {"nad m te X", "non-advective momentum tendency (X component) [m/s**2]"},
    /* 109 */ {"nad m te Y", "non-advective momentum tendency (Y component) [m/s**2]"},
    /* 110 */ {"torque", "sum of mountain and frictional torque [kg*(m/s)**2]"},
    /* 111 */ {"budget val", "budget values [1]"},
    /* 112 */ {"scale fact", "scale factor [1]"},
    /* 113 */ {"Coriol par", "Coriolis parameter [s**(-1)]"},
    /* 114 */ {"PHI", "latitude [degr N]"},
    /* 115 */ {"RLA", "longitude [degr E]"},
    /* 116 */ {"relax fact", "relaxation factor (lateral boundary, LAM) [1]"},
    /* 117 */ {"climsstint", "climatic sea surface temp interpolated in time [degr C]"},
    /* 118 */ {"pot vortic", "potential vorticity [K*m**2/(s*kg)]"},
    /* 119 */ {"ln ps", "log surface pressure [1]"},
    /* 120 */ {"EXP_SI", "undefined"},
    /* 121 */ {"RHS_SI", "undefined"},
    /* 122 */ {"DTTDIV", "undefined"},
    /* 123 */ {"var123", "undefined"},
    /* 124 */ {"var124", "undefined"},
    /* 125 */ {"var125", "undefined"},
    /* 126 */ {"var126", "undefined"},
    /* 127 */ {"var127", "undefined"},
    /* 128 */ {"var128", "undefined"},
    /* 129 */ {"var129", "undefined"},
    /* 130 */ {"var130", "undefined"},
    /* 131 */ {"var131", "undefined"},
    /* 132 */ {"var132", "undefined"},
    /* 133 */ {"var133", "undefined"},
    /* 134 */ {"var134", "undefined"},
    /* 135 */ {"var135", "undefined"},
    /* 136 */ {"var136", "undefined"},
    /* 137 */ {"var137", "undefined"},
    /* 138 */ {"var138", "undefined"},
    /* 139 */ {"var139", "undefined"},
    /* 140 */ {"var140", "undefined"},
    /* 141 */ {"var141", "undefined"},
    /* 142 */ {"var142", "undefined"},
    /* 143 */ {"var143", "undefined"},
    /* 144 */ {"var144", "undefined"},
    /* 145 */ {"var145", "undefined"},
    /* 146 */ {"var146", "undefined"},
    /* 147 */ {"var147", "undefined"},
    /* 148 */ {"var148", "undefined"},
    /* 149 */ {"var149", "undefined"},
    /* 150 */ {"SO2-conc", "SO2-concentration [10**(-6)*g/m**3]"},
    /* 151 */ {"SO2-dryd", "SO2-dry deposition [10**(-3)*g/m**2]"},
    /* 152 */ {"SO2-wetd", "SO2-wet deposition [10**(-3)*g/m**2]"},
    /* 153 */ {"SO4-conc", "SO4-concentration [10**(-6)*g/m**3]"},
    /* 154 */ {"SO4-dryd", "SO4-dry deposition [10**(-3)*g/m**2]"},
    /* 155 */ {"SO4-wetd", "SO4-wet deposition [10**(-3)*g/m**2]"},
    /* 156 */ {"NO-conc", "NO-concentration [10**(-6)*g/m**3]"},
    /* 157 */ {"NO-dryd", "NO-dry deposition [10**(-3)*g/m**2]"},
    /* 158 */ {"NO-wetd", "NO-wet deposition [10**(-3)*g/m**2]"},
    /* 159 */ {"NO2-conc", "NO2-concentration [10**(-6)*g/m**3]"},
    /* 160 */ {"NO2-dryd", "NO2-dry deposition [10**(-3)*g/m**2]"},
    /* 161 */ {"NO2-wetd", "NO2-wet deposition [10**(-3)*g/m**2]"},
    /* 162 */ {"NO3-conc", "NO3-concentration [10**(-6)*g/m**3]"},
    /* 163 */ {"NO3-dryd", "NO3-dry deposition [10**(-3)*g/m**2]"},
    /* 164 */ {"NO3-wetd", "NO3-wet deposition [10**(-3)*g/m**2]"},
    /* 165 */ {"HNO3-conc", "HNO3-concentration [10**(-6)*g/m**3]"},
    /* 166 */ {"HNO3-dryd", "HNO3-dry deposition [10**(-3)*g/m**2]"},
    /* 167 */ {"HNO3-wetd", "HNO3-wet deposition [10**(-3)*g/m**2]"},
    /* 168 */ {"NH3-conc", "NH3-concentration [10**(-6)*g/m**3]"},
    /* 169 */ {"NH3-dryd", "NH3-dry deposition [10**(-3)*g/m**2]"},
    /* 170 */ {"NH3-wetd", "NH3-wet deposition [10**(-3)*g/m**2]"},
    /* 171 */ {"NH4-conc", "NH4-concentration [10**(-6)*g/m**3]"},
    /* 172 */ {"NH4-dryd", "NH4-dry deposition [10**(-3)*g/m**2]"},
    /* 173 */ {"NH4-wetd", "NH4-wet deposition [10**(-3)*g/m**2]"},
    /* 174 */ {"O3-conc", "O3-concentration [10**(-6)*g/m**3]"},
    /* 175 */ {"PAN-conc", "PAN-concentration [10**(-6)*g/m**3]"},
    /* 176 */ {"PAN-dryd", "PAN-dry deposition [10**(-3)*g/m**2]"},
    /* 177 */ {"OH-conc", "OH-concentration [10**(-6)*g/m**3]"},
    /* 178 */ {"O3-dryd", "O3-dry deposition [10**(-3)*g/m**2]"},
    /* 179 */ {"O3-wetd", "O3-wet deposition [10**(-3)*g/m**2]"},
    /* 180 */ {"O3", "O3-mixing ratio [kg/kg]"},
    /* 181 */ {"var181", "undefined"},
    /* 182 */ {"var182", "undefined"},
    /* 183 */ {"var183", "undefined"},
    /* 184 */ {"var184", "undefined"},
    /* 185 */ {"var185", "undefined"},
    /* 186 */ {"var186", "undefined"},
    /* 187 */ {"var187", "undefined"},
    /* 188 */ {"var188", "undefined"},
    /* 189 */ {"var189", "undefined"},
    /* 190 */ {"var190", "undefined"},
    /* 191 */ {"var191", "undefined"},
    /* 192 */ {"var192", "undefined"},
    /* 193 */ {"var193", "undefined"},
    /* 194 */ {"var194", "undefined"},
    /* 195 */ {"var195", "undefined"},
    /* 196 */ {"var196", "undefined"},
    /* 197 */ {"var197", "undefined"},
    /* 198 */ {"var198", "undefined"},
    /* 199 */ {"var199", "undefined"},
    /* 200 */ {"I131-conc", "I131-concentration [Bq/m**3]"},
    /* 201 */ {"I131-dryd", "I131-dry deposition [Bq/m**2]"},
    /* 202 */ {"I131-wetd", "I131-wet deposition [Bq/m**2]"},
    /* 203 */ {"Cs137-conc", "Cs137-concentration [Bq/m**3]"},
    /* 204 */ {"Cs137-dryd", "Cs1370dry deposition [Bq/m**2]"},
    /* 205 */ {"Cs137-wetd", "Cs137-wet deposition [Bq/m**2]"},
    /* 206 */ {"Te132-conc", "Te132-concentration [Bq/m**3]"},
    /* 207 */ {"Te132-dryd", "Te132-dry deposition [Bq/m**2]"},
    /* 208 */ {"Te132-wetd", "Te132-wet deposition [Bq/m**2]"},
    /* 209 */ {"Zr95-conc", "Zr95-concentration [Bq/m**3]"},
    /* 210 */ {"Zr95-dryd", "Zr95-dry deposition [Bq/m**2]"},
    /* 211 */ {"Zr95-wetd", "Zr95-wet deposition [Bq/m**2]"},
    /* 212 */ {"var212", "undefined"},
    /* 213 */ {"var213", "undefined"},
    /* 214 */ {"var214", "undefined"},
    /* 215 */ {"var215", "undefined"},
    /* 216 */ {"var216", "undefined"},
    /* 217 */ {"var217", "undefined"},
    /* 218 */ {"var218", "undefined"},
    /* 219 */ {"var219", "undefined"},
    /* 220 */ {"var220", "undefined"},
    /* 221 */ {"var221", "undefined"},
    /* 222 */ {"var222", "undefined"},
    /* 223 */ {"var223", "undefined"},
    /* 224 */ {"var224", "undefined"},
    /* 225 */ {"var225", "undefined"},
    /* 226 */ {"var226", "undefined"},
    /* 227 */ {"var227", "undefined"},
    /* 228 */ {"var228", "undefined"},
    /* 229 */ {"var229", "undefined"},
    /* 230 */ {"var230", "undefined"},
    /* 231 */ {"var231", "undefined"},
    /* 232 */ {"var232", "undefined"},
    /* 233 */ {"var233", "undefined"},
    /* 234 */ {"var234", "undefined"},
    /* 235 */ {"var235", "undefined"},
    /* 236 */ {"var236", "undefined"},
    /* 237 */ {"var237", "undefined"},
    /* 238 */ {"var238", "undefined"},
    /* 239 */ {"var239", "undefined"},
    /* 240 */ {"UV_Ind_F_h", "UV_Index corr. for albedo+altitude,cloudless(F), h [1]"},
    /* 241 */ {"BasicUV_IF", "Basic UV_Index m.s.l.,fixed albedo,cloudless(F), h [1]"},
    /* 242 */ {"UV_Ind_W_h", "UV_Index corrected for albedo+altitude+clouds(W),h [1]"},
    /* 243 */ {"UV_IndmaxF", "UV_Index cloudless (F), daily maximum [1]"},
    /* 244 */ {"SB-Index", "Sonnenbrand-Index [(W*10**(-3))/m**2]"},
    /* 245 */ {"SB-Index W", "Sonnenbrand-Index bei mittl. Bewoelkung (08z-12z) [(W*10**(-3))/m**2]"},
    /* 246 */ {"Kan.UVB-WI", "Kanadischer UVB-Warnindex (bew|lkungsreduziert) [(W*10**(-3))/m**2]"},
    /* 247 */ {"gesamt O3", "total column ozone (Gesamtozon) [Dobson Unit, DU]"},
    /* 248 */ {"UV_IndmaxW", "UV_Index clouded (W), daily maximum [1]"},
    /* 249 */ {"h UV_IndMx", "time of UV_Index maximum [h UTC]"},
    /* 250 */ {"var250", "undefined"},
    /* 251 */ {"var251", "undefined"},
    /* 252 */ {"var252", "undefined"},
    /* 253 */ {"var253", "undefined"},
    /* 254 */ {"var254", "undefined"},
    /* 255 */ {"var255", "undefined"},
};

/*
 * GRIB table 203 at DWD
 *     Helmut P. Frank, 30.08.2001
 */

struct ParmTable parm_table_dwd_203[256] = {
    /* 0 */ {"var0", "undefined"},
    /* 1 */ {"pressure", "pressure [hPa]"},
    /* 2 */ {"geopot h", "geopotential height [10 * gpm]"},
    /* 3 */ {"var3", "undefined"},
    /* 4 */ {"temperatur", "temperature [1*degree Celsius]"},
    /* 5 */ {"dew-pnt te", "dew-point temperature [1*degree Celsius]"},
    /* 6 */ {"windcompXY", "wind components X/Y (X*100000 + ((Y*10)+5000)) [m/s]"},
    /* 7 */ {"geomet h", "geometrical height [kft]"},
    /* 8 */ {"geomet h", "geometrical height [hft]"},
    /* 9 */ {"wind di/sp", "wind direction and speed (dd*1000 + ff) [1*degree, 1*kt]"},
    /* 10 */ {"3 h pr cha", "3 hour pressure change [Pa/(3*h)]"},
    /* 11 */ {"Schnee-Mge", "Schneemenge [mm]"},
    /* 12 */ {"var12", "undefined"},
    /* 13 */ {"Bod-Wass-G", "Bodenwassergehalt [mm]"},
    /* 14 */ {"var14", "undefined"},
    /* 15 */ {"stab. ind.", "stability index [K]"},
    /* 16 */ {"var16", "undefined"},
    /* 17 */ {"var17", "undefined"},
    /* 18 */ {"var18", "undefined"},
    /* 19 */ {"max wind", "maximum wind velocity [kt]"},
    /* 20 */ {"wind di/sp", "wind direction and speed (dd*1000 + ff) [5*degrees, 1*(m/s)]"},
    /* 21 */ {"wind di/sp", "wind direction and speed (dd*1000 + ff) [5*degrees, 1*kt]"},
    /* 22 */ {"wave di/he", "direction and height of wind waves (dd*1000 + h) [1*degree, 1*cm]"},
    /* 23 */ {"swe. di/he", "direction and height of swell (dd*1000 + h) [1*degree, 1*cm]"},
    /* 24 */ {"wave m d/h", "mean direction and height of waves (dd*1000 + h) [1*degree, 1*cm]"},
    /* 25 */ {"wind speed", "wind speed [kt]"},
    /* 26 */ {"var26", "undefined"},
    /* 27 */ {"wind compX", "wind component X-direction [kt]"},
    /* 28 */ {"wind compY", "wind component Y-direction [kt]"},
    /* 29 */ {"var29", "undefined"},
    /* 30 */ {"var30", "undefined"},
    /* 31 */ {"var31", "undefined"},
    /* 32 */ {"var32", "undefined"},
    /* 33 */ {"abs voradv", "absolute vorticity advection [1/(s**2)]"},
    /* 34 */ {"var34", "undefined"},
    /* 35 */ {"var35", "undefined"},
    /* 36 */ {"var36", "undefined"},
    /* 37 */ {"var37", "undefined"},
    /* 38 */ {"var38", "undefined"},
    /* 39 */ {"var39", "undefined"},
    /* 40 */ {"var40", "undefined"},
    /* 41 */ {"var41", "undefined"},
    /* 42 */ {"vert. vel.", "vertical velocity [hPa/h]"},
    /* 43 */ {"var43", "undefined"},
    /* 44 */ {"var44", "undefined"},
    /* 45 */ {"var45", "undefined"},
    /* 46 */ {"var46", "undefined"},
    /* 47 */ {"var47", "undefined"},
    /* 48 */ {"var48", "undefined"},
    /* 49 */ {"var49", "undefined"},
    /* 50 */ {"var50", "undefined"},
    /* 51 */ {"var51", "undefined"},
    /* 52 */ {"var52", "undefined"},
    /* 53 */ {"var53", "undefined"},
    /* 54 */ {"var54", "undefined"},
    /* 55 */ {"max. temp.", "maximum temperature [1*degree Celsius]"},
    /* 56 */ {"min. temp.", "minimum temperature [1*degree Celsius]"},
    /* 57 */ {"var57", "undefined"},
    /* 58 */ {"clo", "value of isolation of clothes [1]"},
    /* 59 */ {"pmva", "predected mean vote (angepasst) [1]"},
    /* 60 */ {"feeled t", "feeled temperature [1*degree Celsius]"},
    /* 61 */ {"sea temper", "sea temperature [1*degree Celsius]"},
    /* 62 */ {"var62", "undefined"},
    /* 63 */ {"var63", "undefined"},
    /* 64 */ {"var64", "undefined"},
    /* 65 */ {"var65", "undefined"},
    /* 66 */ {"var66", "undefined"},
    /* 67 */ {"var67", "undefined"},
    /* 68 */ {"var68", "undefined"},
    /* 69 */ {"var69", "undefined"},
    /* 70 */ {"var70", "undefined"},
    /* 71 */ {"var71", "undefined"},
    /* 72 */ {"var72", "undefined"},
    /* 73 */ {"var73", "undefined"},
    /* 74 */ {"var74", "undefined"},
    /* 75 */ {"var75", "undefined"},
    /* 76 */ {"var76", "undefined"},
    /* 77 */ {"var77", "undefined"},
    /* 78 */ {"var78", "undefined"},
    /* 79 */ {"var79", "undefined"},
    /* 80 */ {"var80", "undefined"},
    /* 81 */ {"var81", "undefined"},
    /* 82 */ {"var82", "undefined"},
    /* 83 */ {"var83", "undefined"},
    /* 84 */ {"var84", "undefined"},
    /* 85 */ {"var85", "undefined"},
    /* 86 */ {"Globalstr.", "Summe der Globalstrahlung ueber einen Zeitraum [kWh/m**2]"},
    /* 87 */ {"Nied-GW-GE", "Niederschlagsart+Gewitter+Glatteis (T23-i) (0..99) [1]"},
    /* 88 */ {"NiedGW-Art", "Niederschlagsart+Gewitter (T23-intern)     (0..99) [1]"},
    /* 89 */ {"NiedGE-Art", "Niederschlagsart+Glatteis (T23-intern)     (0..99) [1]"},
    /* 90 */ {"NiedBewArt", "Kombination Niederschl.-Bew.-Blautherm. (283..407) [1]"},
    /* 91 */ {"Konv.U-Gr.", "Hoehe der Konvektionsuntergrenze ueber Grund [m]"},
    /* 92 */ {"Nied.-Art", "Niederschlagsart -ww- (T23-intern)         (0..99) [1]"},
    /* 93 */ {"Konv.-Art", "Konvektionsart                              (0..4) [1]"},
    /* 94 */ {"Konv.UG-nn", "Hoehe der Konvektionsuntergrenze ueber nn [m]"},
    /* 95 */ {"var95", "undefined"},
    /* 96 */ {"var96", "undefined"},
    /* 97 */ {"var97", "undefined"},
    /* 98 */ {"var98", "undefined"},
    /* 99 */ {"Wetter(ww)", "Wetter (verschluesselt nach ww-Tabelle"},
    /* 100 */ {"geostr Vor", "geostrophische Vorticity [1/s]"},
    /* 101 */ {"Geo VorAdv", "geostrophische  Vorticityadvektion [1/s**2]"},
    /* 102 */ {"VerGraVoAd", "vert. Gradient der geostr. Vorticityadvektion [m/(kg*s)]"},
    /* 103 */ {"Geo TemAdv", "geostrophische Schichtdickenadvektion [m**3/(kg*s)]"},
    /* 104 */ {"Lap TemAdv", "Kruemmung der geostr. Schichtdickenadvektion [m/(kg*s)]"},
    /* 105 */ {"Omega Forc", "Forcing rechte Seite Omegagleichung [m/(kg*s)]"},
    /* 106 */ {"var106", "undefined"},
    /* 107 */ {"Schichtd.A", "Schichtdicken-Advektion [m**3/(kg*s)]"},
    /* 108 */ {"AdGeVoThWi", "Advektion von geostr. Vorticity mit dem therm Wind [m/(kg*s)]"},
    /* 109 */ {"Wind-Div.", "Winddivergenz [1/s]"},
    /* 110 */ {"Q", "Q-vector direction and speed (dd*1000 + fff*1E13) [5*deg,1E13*m**2/kg/s]"},
    /* 111 */ {"Qx", "Q-Vektor X-Komponente [m**2/(kg*s)]"},
    /* 112 */ {"Qy", "Q-Vektor Y-Komponente [m**2/(kg*s)]"},
    /* 113 */ {"Div Q", "Divergenz Q [m/(kg*s)]"},
    /* 114 */ {"FrontoGeQn", "Frontogenesefunktion, Q isother-senkrecht-Kompon. [m**2/(kg*s)]"},
    /* 115 */ {"Qs (geo)", "Qs (geo),Komp. Q-Vektor parallel zu den Isothermen [m**2/(kg*s)]"},
    /* 116 */ {"DivQn(geo)", "Divergenz Qn  geostrophisch [m/(kg*s)]"},
    /* 117 */ {"DivQs(geo)", "Divergenz Qs  geostrophisch [m/(kg*s)]"},
    /* 118 */ {"Fronto Gen", "Frontogenesefunktion [K**2/(m**2*s)]"},
    /* 119 */ {"var119", "undefined"},
    /* 120 */ {"var120", "undefined"},
    /* 121 */ {"var121", "undefined"},
    /* 122 */ {"var122", "undefined"},
    /* 123 */ {"var123", "undefined"},
    /* 124 */ {"FrontoGenP", "Frontogenese-Parameter [1]"},
    /* 125 */ {"Qs-Vektor", "Qs, Komp. Q-Vektor parallel zu den Isothermen [m**2/(kg*s)]"},
    /* 126 */ {"var126", "undefined"},
    /* 127 */ {"Div Qs", "Divergenz Qs [m/(kg*s)]"},
    /* 128 */ {"var128", "undefined"},
    /* 129 */ {"var129", "undefined"},
    /* 130 */ {"IPV", "Isentrope potentielle Vorticity [K*m**2/(s*kg)]"},
    /* 131 */ {"Wind KompX", "Wind X-Komponente auf isentropen Flaechen [m/s]"},
    /* 132 */ {"Wind KompY", "Wind Y-Komponente auf isentropen Flaechen [m/s]"},
    /* 133 */ {"Druck-Ise.", "Druck einer isentropen Flaeche [hPa]"},
    /* 134 */ {"var134", "undefined"},
    /* 135 */ {"var135", "undefined"},
    /* 136 */ {"var136", "undefined"},
    /* 137 */ {"var137", "undefined"},
    /* 138 */ {"var138", "undefined"},
    /* 139 */ {"var139", "undefined"},
    /* 140 */ {"KO-Index", "KO-Index [K]"},
    /* 141 */ {"TT-Index", "Totals-Totals-Index [K]"},
    /* 142 */ {"S-Index", "S-Index [K]"},
    /* 143 */ {"Stein-Ind", "Steinbeck-Index [1]"},
    /* 144 */ {"Baily-Ind", "Baily-Index [1]"},
    /* 145 */ {"Microburst", "Microburst-Index [1]"},
    /* 146 */ {"Cat-Index", "Clear Air Turbulence Index [1/s]"},
    /* 147 */ {"var147", "undefined"},
    /* 148 */ {"Lab-Energ", "Labilit{tsenergie [J/g]"},
    /* 149 */ {"var149", "undefined"},
    /* 150 */ {"Virt T", "Virtuelle Temperatur [K]"},
    /* 151 */ {"Pseudo T", "Pseudo-Temperatur [K]"},
    /* 152 */ {"Pseudo Pot", "Pseudopotentielle Temperatur [K]"},
    /* 153 */ {"Aequi T", "Aequivalent-Temperatur [K]"},
    /* 154 */ {"Aequi Pot", "Aequivalentpotentielle Temperatur [K]"},
    /* 155 */ {"var155", "undefined"},
    /* 156 */ {"var156", "undefined"},
    /* 157 */ {"var157", "undefined"},
    /* 158 */ {"var158", "undefined"},
    /* 159 */ {"var159", "undefined"},
    /* 160 */ {"Bas St Wol", "Untergrenze strat. Bew|lkung [hft]"},
    /* 161 */ {"Bas St Wol", "Untergrenze strat. Bew|lkung [hPa]"},
    /* 162 */ {"Bas Cu Wol", "Untergrenze cumul. Bew|lkung [hft]"},
    /* 163 */ {"Bas Cu Wol", "Untergrenze cumul. Bew|lkung [hPa]"},
    /* 164 */ {"Top St Wol", "Obergrenze strat. Bew|lkung [hft]"},
    /* 165 */ {"Top St Wol", "Obergrenze strat. Bew|lkung [hPa]"},
    /* 166 */ {"Top Cu Wol", "Obergrenze cumul. Bew|lkung [hft]"},
    /* 167 */ {"Top Cu Wol", "Obergrenze cumul. Bew|lkung [hPa]"},
    /* 168 */ {"var168", "undefined"},
    /* 169 */ {"var169", "undefined"},
    /* 170 */ {"Bas Tur Wo", "Untergrenze Wolkenturbulenz [hft]"},
    /* 171 */ {"Bas Tur Wo", "Untergrenze Wolkenturbulenz [hPa]"},
    /* 172 */ {"Top Tur Wo", "Obergrenze Wolkenturbulenz [hft]"},
    /* 173 */ {"Top Tur Wo", "Obergrenze Wolkenturbulenz [hPa]"},
    /* 174 */ {"Bas Eis Wo", "Untergrenze Vereisung in Wolken [hft]"},
    /* 175 */ {"Bas Eis Wo", "Untergrenze Vereisung in Wolken [hPa]"},
    /* 176 */ {"Top Eis Wo", "Obergrenze Vereisung in Wolken [hft]"},
    /* 177 */ {"Top Eis Wo", "Obergrenze Vereisung in Wolken [hPa]"},
    /* 178 */ {"Int Tur Wo", "Intensitaet der Turbulenz in Wolken  (0..4) [1]"},
    /* 179 */ {"Int Eis Wo", "Intensitaet der Vereisung  (0..4) [1]"},
    /* 180 */ {"var180", "undefined"},
    /* 181 */ {"var181", "undefined"},
    /* 182 */ {"var182", "undefined"},
    /* 183 */ {"var183", "undefined"},
    /* 184 */ {"var184", "undefined"},
    /* 185 */ {"var185", "undefined"},
    /* 186 */ {"var186", "undefined"},
    /* 187 */ {"var187", "undefined"},
    /* 188 */ {"var188", "undefined"},
    /* 189 */ {"var189", "undefined"},
    /* 190 */ {"Sichtweite", "Sichtweite [m]"},
    /* 191 */ {"var191", "undefined"},
    /* 192 */ {"var192", "undefined"},
    /* 193 */ {"var193", "undefined"},
    /* 194 */ {"var194", "undefined"},
    /* 195 */ {"IcingGuess", "Icing Regime 1.Guess(1=gen,2=conv,3=strat,4=freez) [1]"},
    /* 196 */ {"IcingGrade", "Icing Grade (1=LGT,2=MOD,3=SEV) [1]"},
    /* 197 */ {"IcingRegim", "Icing Regime(1=general,2=convect,3=strat,4=freez) [1]"},
    /* 198 */ {"var198", "undefined"},
    /* 199 */ {"var199", "undefined"},
    /* 200 */ {"Gru Wetter", "Wetter - Grundzustand   (ww"},
    /* 201 */ {"Lok Wetter", "Wetter - 1. lokale Abweichung  (ww"},
    /* 202 */ {"Lok Wetter", "Wetter - 2. lokale Abweichung  (ww"},
    /* 203 */ {"CLDEPTH", "cloud depth (grey scale"},
    /* 204 */ {"CLCT_MOD", "modified total cloud cover  (0..1) [1]"},
    /* 205 */ {"curr weath", "current weather (symbol number"},
    /* 206 */ {"var206", "undefined"},
    /* 207 */ {"var207", "undefined"},
    /* 208 */ {"var208", "undefined"},
    /* 209 */ {"var209", "undefined"},
    /* 210 */ {"var210", "undefined"},
    /* 211 */ {"Cu", "Cumulus  (0..1) [1]"},
    /* 212 */ {"Cb", "Cumulimbus  (0..1) [1]"},
    /* 213 */ {"Sc", "Stratocumulus  (0..1) [1]"},
    /* 214 */ {"Ac", "Altocumulus  (0..1) [1]"},
    /* 215 */ {"Ci", "Cirrus  (0..1) [1]"},
    /* 216 */ {"St", "Stratus  (0..1) [1]"},
    /* 217 */ {"As", "Altostratus  (0..1) [1]"},
    /* 218 */ {"var218", "undefined"},
    /* 219 */ {"var219", "undefined"},
    /* 220 */ {"var220", "undefined"},
    /* 221 */ {"Bedeckung", "Bedeckung in Stufen [1]"},
    /* 222 */ {"Konvektion", "Konvektion  ja/nein [1]"},
    /* 223 */ {"MN >90%", "Gesamtbedeckung > 90%  ja/nein [1]"},
    /* 224 */ {"RF700 >89%", "relative Feuchte 700 hPa >= 90%  ja/nein [1]"},
    /* 225 */ {"RR12 zentr", "Niederschlag 12 std. zentriert [mm]"},
    /* 226 */ {"RR12 <=0.5", "Niederschlag 12 std. zentriert, Werte <= 0.5mm [mm]"},
    /* 227 */ {"RR12 SA>60", "RR12 zentriert, Schneeanteil > 60%  ja/nein [1]"},
    /* 228 */ {"RR12 Kv>60", "RR12 zentriert, konvektiver Anteil > 60%  ja/nein [1]"},
    /* 229 */ {"SRR12ff", "Starkniederschlag in Stufen (12 std. Folgezeitr) [1]"},
    /* 230 */ {"RRMAX/STD", "Maximaler Starkniederschlag / std [mm/h]"},
    /* 231 */ {"RRMAX/MIN", "Maximaler Starkniederschlag / min [mm/min]"},
    /* 232 */ {"SN12ff >15", "Schneefall (12std. Folgezeitraum) > 15 mm  ja/nein [1]"},
    /* 233 */ {"RRgefr12ff", "gefrierender Regen (12std. Folgezeitraum)  ja/nein [1]"},
    /* 234 */ {"FFboe", "Boeenstaerke in Stufen [1]"},
    /* 235 */ {"Gewitter", "Gewitter in Stufen [1]"},
    /* 236 */ {"Tx2m12h ze", "2m Maximumtemperatur 12h zentriert [Grad Celsius]"},
    /* 237 */ {"Tn2m12h ze", "2m Minimumtemperatur 12h zentriert [Grad Celsius]"},
    /* 238 */ {"var238", "undefined"},
    /* 239 */ {"var239", "undefined"},
    /* 240 */ {"var240", "undefined"},
    /* 241 */ {"var241", "undefined"},
    /* 242 */ {"var242", "undefined"},
    /* 243 */ {"var243", "undefined"},
    /* 244 */ {"var244", "undefined"},
    /* 245 */ {"var245", "undefined"},
    /* 246 */ {"var246", "undefined"},
    /* 247 */ {"var247", "undefined"},
    /* 248 */ {"var248", "undefined"},
    /* 249 */ {"var249", "undefined"},
    /* 250 */ {"var250", "undefined"},
    /* 251 */ {"SCHWUELIND", "Schwuele-Index [1]"},
    /* 252 */ {"SMOGSTUFEN", "Smog-Intensitaetsstufen [1]"},
    /* 253 */ {"var253", "undefined"},
    /* 254 */ {"SMOGHOEHE", "Obergrenze Smog  ( Inversionshoehe ) [m]"},
    /* 255 */ {"var255", "undefined"},
};

struct ParmTable parm_table_cptec_254[256] = {
   /* 0 */ {"var0", "undefined"},
   /* 1 */ {"PRES", "Pressure [hPa]"},
   /* 2 */ {"psnm", "Pressure reduced to MSL [hPa]"},
   /* 3 */ {"tsps", "Pressure tendency [Pa/s]"},
   /* 4 */ {"var4", "undefined"},
   /* 5 */ {"var5", "undefined"},
   /* 6 */ {"geop", "Geopotential [dam]"},
   /* 7 */ {"zgeo", "Geopotential height [gpm]"},
   /* 8 */ {"gzge", "Geometric height [m]"},
   /* 9 */ {"var9", "undefined"},
   /* 10 */ {"var10", "undefined"},
   /* 11 */ {"temp", "ABSOLUTE TEMPERATURE [K]"},
   /* 12 */ {"vtmp", "VIRTUAL TEMPERATURE [K]"},
   /* 13 */ {"ptmp", "POTENTIAL TEMPERATURE [K]"},
   /* 14 */ {"psat", "PSEUDO-ADIABATIC POTENTIAL TEMPERATURE [K]"},
   /* 15 */ {"mxtp", "MAXIMUM TEMPERATURE [K]"},
   /* 16 */ {"mntp", "MINIMUM TEMPERATURE [K]"},
   /* 17 */ {"tpor", "DEW POINT TEMPERATURE [K]"},
   /* 18 */ {"dptd", "DEW POINT DEPRESSION [K]"},
   /* 19 */ {"lpsr", "LAPSE RATE [K/m]"},
   /* 20 */ {"var20", "undefined"},
   /* 21 */ {"rds1", "RADAR SPECTRA(1) [non-dim]"},
   /* 22 */ {"rds2", "RADAR SPECTRA(2) [non-dim]"},
   /* 23 */ {"rds3", "RADAR SPECTRA(3) [non-dim]"},
   /* 24 */ {"var24", "undefined"},
   /* 25 */ {"tpan", "TEMPERATURE ANOMALY [K]"},
   /* 26 */ {"psan", "PRESSURE ANOMALY [Pa hPa]"},
   /* 27 */ {"zgan", "GEOPOT HEIGHT ANOMALY [m]"},
   /* 28 */ {"wvs1", "WAVE SPECTRA(1) [non-dim]"},
   /* 29 */ {"wvs2", "WAVE SPECTRA(2) [non-dim]"},
   /* 30 */ {"wvs3", "WAVE SPECTRA(3) [non-dim]"},
   /* 31 */ {"wind", "WIND DIRECTION  [deg]"},
   /* 32 */ {"wins", "WIND SPEED [m/s]"},
   /* 33 */ {"uvel", "ZONAL WIND (U) [m/s]"},
   /* 34 */ {"vvel", "MERIDIONAL WIND (V) [m/s]"},
   /* 35 */ {"fcor", "STREAM FUNCTION [m2/s]"},
   /* 36 */ {"potv", "VELOCITY POTENTIAL [m2/s]"},
   /* 37 */ {"var37", "undefined"},
   /* 38 */ {"sgvv", "SIGMA COORD VERT VEL [sec/sec]"},
   /* 39 */ {"omeg", "OMEGA [Pa/s]"},
   /* 40 */ {"omg2", "VERTICAL VELOCITY [m/s]"},
   /* 41 */ {"abvo", "ABSOLUTE VORTICITY        [10**5/sec]"},
   /* 42 */ {"abdv", "ABSOLUTE DIVERGENCE [10**5/sec]"},
   /* 43 */ {"vort", "VORTICITY  [1/s]"},
   /* 44 */ {"divg", "DIVERGENCE [1/s]"},
   /* 45 */ {"vucs", "VERTICAL U-COMP SHEAR [1/sec]"},
   /* 46 */ {"vvcs", "VERT V-COMP SHEAR [1/sec]"},
   /* 47 */ {"dirc", "DIRECTION OF CURRENT [deg]"},
   /* 48 */ {"spdc", "SPEED OF CURRENT [m/s]"},
   /* 49 */ {"ucpc", "U-COMPONENT OF CURRENT [m/s]"},
   /* 50 */ {"vcpc", "V-COMPONENT OF CURRENT [m/s]"},
   /* 51 */ {"umes", "SPECIFIC HUMIDITY [kg/kg]"},
   /* 52 */ {"umrl", "RELATIVE HUMIDITY [no Dim]"},
   /* 53 */ {"hmxr", "HUMIDITY MIXING RATIO [kg/kg]"},
   /* 54 */ {"agpl", "INST. PRECIPITABLE WATER [Kg/m2]"},
   /* 55 */ {"vapp", "VAPOUR PRESSURE [Pa hpa]"},
   /* 56 */ {"sadf", "SATURATION DEFICIT        [Pa hPa]"},
   /* 57 */ {"evap", "EVAPORATION [Kg/m2/day]"},
   /* 58 */ {"var58", "undefined"},
   /* 59 */ {"prcr", "PRECIPITATION RATE        [kg/m2/day]"},
   /* 60 */ {"thpb", "THUNDER PROBABILITY [%]"},
   /* 61 */ {"prec", "TOTAL PRECIPITATION [Kg/m2/day]"},
   /* 62 */ {"prge", "LARGE SCALE PRECIPITATION [Kg/m2/day]"},
   /* 63 */ {"prcv", "CONVECTIVE PRECIPITATION [Kg/m2/day]"},
   /* 64 */ {"neve", "SNOWFALL [Kg/m2/day]"},
   /* 65 */ {"wenv", "WAT EQUIV ACC SNOW DEPTH [kg/m2]"},
   /* 66 */ {"nvde", "SNOW DEPTH        [cm]"},
   /* 67 */ {"mxld", "MIXED LAYER DEPTH [m cm]"},
   /* 68 */ {"tthd", "TRANS THERMOCLINE DEPTH [m cm]"},
   /* 69 */ {"mthd", "MAIN THERMOCLINE DEPTH [m cm]"},
   /* 70 */ {"mtha", "MAIN THERMOCLINE ANOM [m cm]"},
   /* 71 */ {"cbnv", "CLOUD COVER [0-1]"},
   /* 72 */ {"cvnv", "CONVECTIVE CLOUD COVER [0-1]"},
   /* 73 */ {"lwnv", "LOW CLOUD COVER [0-1]"},
   /* 74 */ {"mdnv", "MEDIUM CLOUD COVER        [0-1]"},
   /* 75 */ {"hinv", "HIGH CLOUD COVER [0-1]"},
   /* 76 */ {"wtnv", "CLOUD WATER [kg/m2]"},
   /* 77 */ {"bli", "BEST LIFTED INDEX (TO 500 HPA) [K]"},
   /* 78 */ {"var78", "undefined"},
   /* 79 */ {"var79", "undefined"},
   /* 80 */ {"var80", "undefined"},
   /* 81 */ {"lsmk", "LAND SEA MASK [0,1]"},
   /* 82 */ {"dslm", "DEV SEA_LEV FROM MEAN [m]"},
   /* 83 */ {"zorl", "ROUGHNESS LENGTH [m]"},
   /* 84 */ {"albe", "ALBEDO [%]"},
   /* 85 */ {"dstp", "DEEP SOIL TEMPERATURE [K]"},
   /* 86 */ {"soic", "SOIL MOISTURE CONTENT [Kg/m2]"},
   /* 87 */ {"vege", "VEGETATION        [%]"},
   /* 88 */ {"var88", "undefined"},
   /* 89 */ {"dens", "DENSITY [kg/m3]"},
   /* 90 */ {"var90", "Undefined"},
   /* 91 */ {"icec", "ICE CONCENTRATION [fraction]"},
   /* 92 */ {"icet", "ICE THICKNESS [m]"},
   /* 93 */ {"iced", "DIRECTION OF ICE DRIFT [deg]"},
   /* 94 */ {"ices", "SPEED OF ICE DRIFT        [m/s]"},
   /* 95 */ {"iceu", "U-COMP OF ICE DRIFT [m/s]"},
   /* 96 */ {"icev", "V-COMP OF ICE DRIFT [m/s]"},
   /* 97 */ {"iceg", "ICE GROWTH        [m]"},
   /* 98 */ {"icdv", "ICE DIVERGENCE [sec/sec]"},
   /* 99 */ {"var99", "undefined"},
   /* 100 */ {"shcw", "SIG HGT COM WAVE/SWELL [m]"},
   /* 101 */ {"wwdi", "DIRECTION OF WIND WAVE [deg]"},
   /* 102 */ {"wwsh", "SIG HGHT OF WIND WAVES [m]"},
   /* 103 */ {"wwmp", "MEAN PERIOD WIND WAVES [sec]"},
   /* 104 */ {"swdi", "DIRECTION OF SWELL WAVE [deg]"},
   /* 105 */ {"swsh", "SIG HEIGHT SWELL WAVES [m]"},
   /* 106 */ {"swmp", "MEAN PERIOD SWELL WAVES [sec]"},
   /* 107 */ {"prwd", "PRIMARY WAVE DIRECTION [deg]"},
   /* 108 */ {"prmp", "PRIM WAVE MEAN PERIOD [s]"},
   /* 109 */ {"swdi", "SECOND WAVE DIRECTION [deg]"},
   /* 110 */ {"swmp", "SECOND WAVE MEAN PERIOD [s]"},
   /* 111 */ {"ocas", "SHORT WAVE ABSORBED AT GROUND [W/m2]"},
   /* 112 */ {"slds", "NET LONG WAVE AT BOTTOM [W/m2]"},
   /* 113 */ {"nswr", "NET SHORT-WAV RAD(TOP) [W/m2]"},
   /* 114 */ {"role", "OUTGOING LONG WAVE AT TOP [W/m2]"},
   /* 115 */ {"lwrd", "LONG-WAV RAD [W/m2]"},
   /* 116 */ {"swea", "SHORT WAVE ABSORBED BY EARTH/ATMOSPHERE  [W/m2]"},
   /* 117 */ {"glbr", "GLOBAL RADIATION [W/m2 ]"},
   /* 118 */ {"var118", "undefined"},
   /* 119 */ {"var119", "undefined"},
   /* 120 */ {"var120", "undefined"},
   /* 121 */ {"clsf", "LATENT HEAT FLUX FROM SURFACE [W/m2]"},
   /* 122 */ {"cssf", "SENSIBLE HEAT FLUX FROM SURFACE [W/m2]"},
   /* 123 */ {"blds", "BOUND LAYER DISSIPATION [W/m2]"},
   /* 124 */ {"var124", "undefined"},
   /* 125 */ {"var125", "undefined"},
   /* 126 */ {"var126", "undefined"},
   /* 127 */ {"imag", "IMAGE [image^data]"},
   /* 128 */ {"tp2m", "2 METRE TEMPERATURE [K]"},
   /* 129 */ {"dp2m", "2 METRE DEWPOINT TEMPERATURE [K]"},
   /* 130 */ {"u10m", "10 METRE U-WIND COMPONENT [m/s]"},
   /* 131 */ {"v10m", "10 METRE V-WIND COMPONENT [m/s]"},
   /* 132 */ {"topo", "TOPOGRAPHY [m]"},
   /* 133 */ {"gsfp", "GEOMETRIC MEAN SURFACE PRESSURE [hPa]"},
   /* 134 */ {"lnsp", "LN SURFACE PRESSURE [hPa]"},
   /* 135 */ {"pslc", "SURFACE PRESSURE [hPa]"},
   /* 136 */ {"pslm", "M S L PRESSURE (MESINGER METHOD) [hPa]"},
   /* 137 */ {"mask", "MASK  [-/+]"},
   /* 138 */ {"mxwu", "MAXIMUM U-WIND [m/s]"},
   /* 139 */ {"mxwv", "MAXIMUM V-WIND [m/s]"},
   /* 140 */ {"cape", "CONVECTIVE AVAIL. POT.ENERGY [m2/s2]"},
   /* 141 */ {"cine", "CONVECTIVE INHIB. ENERGY [m2/s2]"},
   /* 142 */ {"lhcv", "CONVECTIVE LATENT HEATING [K/s]"},
   /* 143 */ {"mscv", "CONVECTIVE MOISTURE SOURCE [1/s]"},
   /* 144 */ {"scvm", "SHALLOW CONV. MOISTURE SOURCE [1/s]"},
   /* 145 */ {"scvh", "SHALLOW CONVECTIVE HEATING [K/s]"},
   /* 146 */ {"mxwp", "MAXIMUM WIND PRESS. LVL  [hPa]"},
   /* 147 */ {"ustr", "STORM MOTION U-COMPONENT [m/s]"},
   /* 148 */ {"vstr", "STORM MOTION V-COMPONENT [m/s]"},
   /* 149 */ {"cbnt", "MEAN CLOUD COVER [0-1]"},
   /* 150 */ {"pcbs", "PRESSURE AT CLOUD BASE [hPa]"},
   /* 151 */ {"pctp", "PRESSURE AT CLOUD TOP [hPa]"},
   /* 152 */ {"fzht", "FREEZING LEVEL HEIGHT [m]"},
   /* 153 */ {"fzrh", "FREEZING LEVEL RELATIVE HUMIDITY [%]"},
   /* 154 */ {"fdlt", "FLIGHT LEVELS TEMPERATURE [K]"},
   /* 155 */ {"fdlu", "FLIGHT LEVELS U-WIND [m/s]"},
   /* 156 */ {"fdlv", "FLIGHT LEVELS V-WIND [m/s]"},
   /* 157 */ {"tppp", "TROPOPAUSE PRESSURE   [hPa]"},
   /* 158 */ {"tppt", "TROPOPAUSE TEMPERATURE [K]"},
   /* 159 */ {"tppu", "TROPOPAUSE U-WIND COMPONENT [m/s]"},
   /* 160 */ {"tppv", "TROPOPAUSE v-WIND COMPONENT [m/s]"},
   /* 161 */ {"var161", "undefined"},
   /* 162 */ {"gvdu", "GRAVITY WAVE DRAG DU/DT [m/s2]"},
   /* 163 */ {"gvdv", "GRAVITY WAVE DRAG DV/DT [m/s2]"},
   /* 164 */ {"gvus", "GRAVITY WAVE DRAG SFC ZONAL STRESS  [Pa]"},
   /* 165 */ {"gvvs", "GRAVITY WAVE DRAG SFC MERIDIONAL STRESS [Pa]"},
   /* 166 */ {"var166", "undefined"},
   /* 167 */ {"dvsh", "DIVERGENCE OF SPECIFIC HUMIDITY [1/s]"},
   /* 168 */ {"hmfc", "HORIZ. MOISTURE FLUX CONV.  [1/s]"},
   /* 169 */ {"vmfl", "VERT. INTEGRATED MOISTURE FLUX CONV. [kg/(m2*s)]"},
   /* 170 */ {"vadv", "VERTICAL MOISTURE ADVECTION  [kg/(kg*s)]"},
   /* 171 */ {"nhcm", "NEG. HUM. CORR. MOISTURE SOURCE [kg/(kg*s)]"},
   /* 172 */ {"lglh", "LARGE SCALE LATENT HEATING   [K/s]"},
   /* 173 */ {"lgms", "LARGE SCALE MOISTURE SOURCE  [1/s]"},
   /* 174 */ {"smav", "SOIL MOISTURE AVAILABILITY  [0-1]"},
   /* 175 */ {"tgrz", "SOIL TEMPERATURE OF ROOT ZONE [K]"},
   /* 176 */ {"bslh", "BARE SOIL LATENT HEAT [Ws/m2]"},
   /* 177 */ {"evpp", "POTENTIAL SFC EVAPORATION [m]"},
   /* 178 */ {"rnof", "RUNOFF [kg/m2/s)]"},
   /* 179 */ {"pitp", "INTERCEPTION LOSS [W/m2]"},
   /* 180 */ {"vpca", "VAPOR PRESSURE OF CANOPY AIR SPACE [mb]"},
   /* 181 */ {"qsfc", "SURFACE SPEC HUMIDITY   [kg/kg]"},
   /* 182 */ {"ussl", "SOIL WETNESS OF SURFACE [0-1]"},
   /* 183 */ {"uzrs", "SOIL WETNESS OF ROOT ZONE [0-1]"},
   /* 184 */ {"uzds", "SOIL WETNESS OF DRAINAGE ZONE [0-1]"},
   /* 185 */ {"amdl", "STORAGE ON CANOPY [m]"},
   /* 186 */ {"amsl", "STORAGE ON GROUND [m]"},
   /* 187 */ {"tsfc", "SURFACE TEMPERATURE [K]"},
   /* 188 */ {"tems", "SURFACE ABSOLUTE TEMPERATURE [K]"},
   /* 189 */ {"tcas", "TEMPERATURE OF CANOPY AIR SPACE [K]"},
   /* 190 */ {"ctmp", "TEMPERATURE AT CANOPY [K]"},
   /* 191 */ {"tgsc", "GROUND/SURFACE COVER TEMPERATURE [K]"},
   /* 192 */ {"uves", "SURFACE ZONAL WIND (U) [m/s]"},
   /* 193 */ {"usst", "SURFACE ZONAL WIND STRESS [Pa]"},
   /* 194 */ {"vves", "SURFACE MERIDIONAL WIND (V) [m/s]"},
   /* 195 */ {"vsst", "SURFACE MERIDIONAL WIND STRESS [Pa]"},
   /* 196 */ {"suvf", "SURFACE MOMENTUM FLUX [W/m2]"},
   /* 197 */ {"iswf", "INCIDENT SHORT WAVE FLUX [W/m2]"},
   /* 198 */ {"ghfl", "TIME AVE GROUND HT FLX   [W/m2]"},
   /* 199 */ {"var199", "undefined"},
   /* 200 */ {"lwbc", "NET LONG WAVE AT BOTTOM (CLEAR) [W/m2]"},
   /* 201 */ {"lwtc", "OUTGOING LONG WAVE AT TOP (CLEAR) [W/m2]"},
   /* 202 */ {"swec", "SHORT WV ABSRBD BY EARTH/ATMOS (CLEAR) [W/m2]"},
   /* 203 */ {"ocac", "SHORT WAVE ABSORBED AT GROUND (CLEAR) [W/m2]"},
   /* 204 */ {"var204", "undefined"},
   /* 205 */ {"lwrh", "LONG WAVE RADIATIVE HEATING  [K/s]"},
   /* 206 */ {"swrh", "SHORT WAVE RADIATIVE HEATING [K/s]"},
   /* 207 */ {"olis", "DOWNWARD LONG WAVE AT BOTTOM [W/m2]"},
   /* 208 */ {"olic", "DOWNWARD LONG WAVE AT BOTTOM (CLEAR) [W/m2]"},
   /* 209 */ {"ocis", "DOWNWARD SHORT WAVE AT GROUND [W/m2]"},
   /* 210 */ {"ocic", "DOWNWARD SHORT WAVE AT GROUND (CLEAR) [W/m2]"},
   /* 211 */ {"oles", "UPWARD LONG WAVE AT BOTTOM [W/m2]"},
   /* 212 */ {"oces", "UPWARD SHORT WAVE AT GROUND [W/m2]"},
   /* 213 */ {"swgc", "UPWARD SHORT WAVE AT GROUND (CLEAR) [W/m2]"},
   /* 214 */ {"roce", "UPWARD SHORT WAVE AT TOP [W/m2]"},
   /* 215 */ {"swtc", "UPWARD SHORT WAVE AT TOP (CLEAR) [W/m2]"},
   /* 216 */ {"var216", "undefined"},
   /* 217 */ {"var217", "undefined"},
   /* 218 */ {"hhdf", "HORIZONTAL HEATING DIFFUSION [K/s]"},
   /* 219 */ {"hmdf", "HORIZONTAL MOISTURE DIFFUSION [1/s]"},
   /* 220 */ {"hddf", "HORIZONTAL DIVERGENCE DIFFUSION [1/s2]"},
   /* 221 */ {"hvdf", "HORIZONTAL VORTICITY DIFFUSION [1/s2]"},
   /* 222 */ {"vdms", "VERTICAL DIFF. MOISTURE SOURCE [1/s]"},
   /* 223 */ {"vdfu", "VERTICAL DIFFUSION DU/DT [m/s2]"},
   /* 224 */ {"vdfv", "VERTICAL DIFFUSION DV/DT [m/s2]"},
   /* 225 */ {"vdfh", "VERTICAL DIFFUSION HEATING [K/s]"},
   /* 226 */ {"umrs", "SURFACE RELATIVE HUMIDITY [no Dim]"},
   /* 227 */ {"vdcc", "VERTICAL DIST TOTAL CLOUD COVER [no Dim]"},
   /* 228 */ {"var228", "undefined"},
   /* 229 */ {"var229", "undefined"},
   /* 230 */ {"usmt", "TIME MEAN SURFACE ZONAL WIND (U) [m/s]"},
   /* 231 */ {"vsmt", "TIME MEAN SURFACE MERIDIONAL WIND (V) [m/s]"},
   /* 232 */ {"tsmt", "TIME MEAN SURFACE ABSOLUTE TEMPERATURE [K]"},
   /* 233 */ {"rsmt", "TIME MEAN SURFACE RELATIVE HUMIDITY [no Dim]"},
   /* 234 */ {"atmt", "TIME MEAN ABSOLUTE TEMPERATURE [K]"},
   /* 235 */ {"stmt", "TIME MEAN DEEP SOIL TEMPERATURE [K]"},
   /* 236 */ {"ommt", "TIME MEAN DERIVED OMEGA [Pa/s]"},
   /* 237 */ {"dvmt", "TIME MEAN DIVERGENCE [1/s]"},
   /* 238 */ {"zhmt", "TIME MEAN GEOPOTENTIAL HEIGHT [m]"},
   /* 239 */ {"lnmt", "TIME MEAN LOG SURFACE PRESSURE [ln(cbar)]"},
   /* 240 */ {"mkmt", "TIME MEAN MASK [-/+]"},
   /* 241 */ {"vvmt", "TIME MEAN MERIDIONAL WIND (V) [m/s]"},
   /* 242 */ {"omtm", "TIME MEAN OMEGA  [cbar/s]"},
   /* 243 */ {"ptmt", "TIME MEAN POTENTIAL TEMPERATURE [K]"},
   /* 244 */ {"pcmt", "TIME MEAN PRECIP. WATER  [kg/m2]"},
   /* 245 */ {"rhmt", "TIME MEAN RELATIVE HUMIDITY [%]"},
   /* 246 */ {"mpmt", "TIME MEAN SEA LEVEL PRESSURE [hPa]"},
   /* 247 */ {"simt", "TIME MEAN SIGMADOT [1/s]"},
   /* 248 */ {"uemt", "TIME MEAN SPECIFIC HUMIDITY [kg/kg]"},
   /* 249 */ {"fcmt", "TIME MEAN STREAM FUNCTION| m2/s]"},
   /* 250 */ {"psmt", "TIME MEAN SURFACE PRESSURE [hPa]"},
   /* 251 */ {"tmmt", "TIME MEAN SURFACE TEMPERATURE [K]"},
   /* 252 */ {"pvmt", "TIME MEAN VELOCITY POTENTIAL [m2/s]"},
   /* 253 */ {"tvmt", "TIME MEAN VIRTUAL TEMPERATURE [K]"},
   /* 254 */ {"vtmt", "TIME MEAN VORTICITY [1/s]"},
   /* 255 */ {"uvmt", "TIME MEAN ZONAL WIND (U) [m/s]"},
};

