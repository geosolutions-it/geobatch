% AIR_SEA: Introduction to the AIR_SEA TOOLBOX

%              AIR_SEA TOOLBOX (version 2.0: 8/9/99)
%
% 1) Introduction: Welcome to the AIR_SEA toolbox, a collection of MATLAB
% programs (m-files) which can be used to compute surface wind stress and 
% heat flux components from buoy and shipboard atmospheric and near-surface
% oceanographic time series measurements. All m-files include a header
% which describes the mfile's function, input and output variables, and 
% key references when important. They have been written for use with 
% MATLAB 5. 
%
% 2) Conventions:  While not required for many m-files, it is generally
% assumed that the input time series of measured variables are hourly 
% averaged column or row vectors and the other input variables are scalars, 
% all expressed in MKS units. Two time conventions are used: a) decimal 
% Julian yearday where 0000 UT Jan 1 equals 0.0, and b) calender yearday 
% where Jan 1 equals 1. The choice of which convention is used is 
% internally consistent between m-files.
%
% 3) Programs used to compute heat flux components:
%
%    Shortwave flux:
%
%        SWHF: computes net sw flux into ocean and albedo. Uses 
%              SORADNA1 and ALBEDO to compute solar altitude, no-sky 
%              insolation and albedo.
%        
%        SORADNA1: computes no-sky insolation and solar altitude at
%              a given time and location.
%        
%        ALBEDO: computes ocean albedo following Payne (1972).
%
%    Longwave flux:
%
%        LWHF: computes net lw flux into ocean when downward 
%              lw radiation is measured, using Dickey et al (1994).
%
%        BLWHF: computes net lw flux into the ocean when downward
%              lw radiation is NOT measured. Uses SATVAP. Requires
%              as input a cloudiness correction factor from CLOUDCOR.
%
%        CLOUDCOR: cloudiness correction factor used in bulk formulae,
%              based on estimated Cloud Fraction, which is either observed
%              directly or estimated, using, e.g., REEDCF.
%
%        REEDCF: computes daily average Cloud Fraction using formula of 
%              Reed (1977), who relates daily average cloudiness to the observed 
%              reduction in solar insolation from clear-sky values.
%
%    Sensible and latent fluxes:
%
%        HFBULKTC: uses a simplified version of Fairall et al (1996)
%              TOGA/COARE code to compute sensible and latent heat 
%              fluxes into ocean. Present version includes a) Rogers' 
%              weighting factor for unstable conditions, b) the effects 
%              of gustiness, c) a constant marine boundary layer height,
%              d) a limit of zr/L <=3.0 to ensure that the code converges
%              to nonzero stress and heat flux values for strongly stable 
%              conditions, e) cool-skin effect, and f) Webb correction for 
%              latent heat flux.  NOTE: both cool-skin and Webb correction
%              are optional, and user must decide if they want these used,
%              e.g., in SLHFTC. Warm layer effects are not included in this 
%              version.  Uses VISCAIR and QSAT to compute air viscosity 
%              and saturation specific humidity, CDNTC the neutral drag 
%              coefficient, and PSIUTC and PSITTC to adjust the different 
%              transfer coefficients to the measurement heights for a 
%              given stability. Also returns related variables.
%
%        SLHFTC: includes ocean surface current and HFBULKTC to comput
%              sensible and latent heat fluxes into ocean.
%        
%        RAIN_FLUX: computes heat flux and momentum flux due to rain. 
%
% 4) Programs relating wind speed, height, and surface stress.
%
%    Neutral conditions:  
%
%        CDNLP: computes neutral Cd, 10m wind following Large and Pond (1981).
%        CDNTC: computes neutral Cd, 10m wind following Smith (1988).
%        CDNVE: computes neutral Cd, 10m wind following Vera (1983).
%
%        STRESSLP: computes the neutral wind stress using Large and Pond.
%        STRESSTC: computes the neutral wind stress following Smith.
%        STRESSVE: computes the neutral wind stress using Vera.
%
%        SPSHFTLP: computes winds at another height using Large&Pond drag.
%        SPSHFTTC: computes winds at another height using Smith drag.
%        SPSHFTVE: computes winds at another height using Vera drag.
%
%    Non-neutral conditions:
%
%        HFBULKTC: uses simplified version of Fairall et al (1996)
%              TOGA/COARE code to compute surface wind stress amplitude,
%              (Uses Monin-Obukov similarity theory with surface rougness using
%              Charnock approach, like Smith (1988)).
%
%        SLHFTC: includes ocean surface current and HFBULKTC to 
%              compute surface wind stress vector as well as scalar parameters.
%
% 5) Programs used to estimate wave effects on the measured wind speed:
% 
%        WAVEDIST: estimate true wind speed at 10-m height.
%        WAVEDIS1: estimate true wind speed at measurement height.
%        WAVEDIS2: plots wave effects at measurement height vs. wave height.
%        OMEGALMC: estimates wave effect on wind log profile.
%        CDNVE: computes neutral drag coefficient following Vera (1983).
%
%        See WDNOTES for additional information.
%
% 6) Other useful programs:
%
%        AS_CONSTS: contains various constants used in the toolbox.
%
%        DAVEALB: computes daily mean albedo.
%        SUNRISE: computes GMT time of sunrise and sunset (uses SORADNA1).
%
%        GREG2: converts decimal yearday into Julian calendar day.
%        JULIANMD: converts Gregorian calendar dates to decimal Julian day
%                  for days beginning at midnight UT
%        YEARDAY: converts calender month and day into yearday.
%
%        DELQ: air-sea specific humidity difference.
%        EP: net precipitation and evaporation accumulation.
%        QSAT: saturation specific humidity.
%        RELHUMID: relative humidity from wet/dry bulb thermometers.
%        RHADJ: adjusts RH for values above 100.
%        SATVAP: saturation vapour pressure.
%        VAPOR: heat of evaporation.
%        VISCAIR: viscosity of air at a given temperature.
%        COOL_SKIN: computes cool-skin parameters. 
%        T_HFBULKTC: tests HFBULKTC with COARE data.  
%   
% 7) See CONTENTS for listing of all m-files in this toolbox.
%
% 8) History:
%
%    Version 1.0:
%
%    The initial assembly of this toolbox was a collaborative effort
%    by Bob Beardsley (WHOI), Ed Dever (SIO), Steve Lentz (WHOI), Jim 
%    Edson (WHOI), and Dick Payne (WHOI), with additional input from 
%    Steve Anderson (WHOI), Jay Austin (WHOI), Chris Fairall (NOAA),
%    Carl Friehe (UCI), Bill Large (NCAR), Dave Rogers (SIO), Rich 
%    Signell (USGS), and Bob Weller (WHOI). Their input was very useful. 
%   
%    Version 1.1:
%
%    Rich Pawlowicz (UBC) then converted the original version 1.0 
%    (written for MATLAB 4) into a much improved version 1.1 (optimized 
%    for MATLAB 5) which included major coding improvements, the addition 
%    of some new m-files, and some corrections of existing m-files.   
%
%    Version 1.2:
%
%    Ayal Anis (U. Dalhousie) then modified HFBULKTC to include the 
%    Fairall et al (1996) cool-skin effect and Webb correction to the 
%    latent heat flux, plus added files to test the code with COARE 
%    data. Ayal and R. Onken (NATO) also contributed several other files.
%
%    Version 2.0:
%
%    Bob Beardsley has added several m-files and made simple changes
%    to the various m-files to standardize the format and documentation.
%
% 9) Comments, Suggestions, and Improvements
%
%    Please contact Bob Beardsley at rbeardsley@whoi.edu with questions 
%    and comments, especially concerning bugs (and their possible fixes), 
%    ideas for additional m-files, plus any m-files which you want to 
%    contribute to this toolbox. Your help in improving this toolbox will
%    be greatly appreciated.
%
%    As new or/or improved m-files are developed for this toolbox, they 
%    will be added to the AIR_SEA toolbox folder located at the SEA-MAT 
%    Web site (crusty.usgs.gov/sea-mat/).  SEA-MAT is a collection of 
%    MATLAB mfiles for the display and analysis of oceanographic data.
%
%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%% 

