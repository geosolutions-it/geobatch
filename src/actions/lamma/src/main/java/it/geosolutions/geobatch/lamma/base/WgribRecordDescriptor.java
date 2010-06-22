/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://geobatch.codehaus.org/
 *  Copyright (C) 2007-2008-2009 GeoSolutions S.A.S.
 *  http://www.geo-solutions.it
 *
 *  GPLv3 + Classpath exception
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.geosolutions.geobatch.lamma.base;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Date;

/**
 * Comments here ...
 *
 * @author Alessio Fabiani, GeoSolutions S.A.S.
 *
 */
public class WgribRecordDescriptor {
	
	private Long rId;
	
	private Date baseTime;
	
	private String paramId;

	private String paramDescription;

	private String level;
	
	private String forecastTime;

	/**
	 * @param rId the rId to set
	 */
	public void setrId(Long rId) {
		this.rId = rId;
	}

	/**
	 * @return the rId
	 */
	public Long getrId() {
		return rId;
	}

	/**
	 * @param baseTime the baseTime to set
	 */
	public void setBaseTime(Date baseTime) {
		this.baseTime = baseTime;
	}

	/**
	 * @return the baseTime
	 */
	public Date getBaseTime() {
		return baseTime;
	}

	/**
	 * @param paramId the paramId to set
	 */
	public void setParamId(String paramId) {
		this.paramId = paramId;
	}

	/**
	 * @return the paramId
	 */
	public String getParamId() {
		return paramId;
	}

	/**
	 * @param paramDescription the paramDescription to set
	 */
	public void setParamDescription(String paramDescription) {
		this.paramDescription = paramDescription;
	}

	/**
	 * @return the paramDescription
	 */
	public String getParamDescription() {
		return paramDescription;
	}

	/**
	 * @param level the level to set
	 */
	public void setLevel(String level) {
		this.level = level;
	}

	/**
	 * @return the level
	 */
	public String getLevel() {
		String elevationNumber = null;
		String[] parts = level.split(" ");
		
		DecimalFormatSymbols ds = new DecimalFormatSymbols();
		ds.setDecimalSeparator('.');
		DecimalFormat df = new DecimalFormat("0000.000", ds);
		
		if (parts[0].indexOf("-") > 0) {
			elevationNumber = df.format(Double.parseDouble(parts[0].split("-")[0]));
		} else {
			try {
				elevationNumber = df.format(Double.parseDouble(parts[0]));
			} catch (NumberFormatException e) {
				elevationNumber = "0000.000";
			}
		}
		
		return elevationNumber;
	}

	/**
	 * @return the level
	 */
	public String getRawLevel() {
		return this.level;
	}
	
	/**
	 * @param forecastTime the forecastTime to set
	 */
	public void setForecastTime(String forecastTime) {
		this.forecastTime = forecastTime;
	}

	/**
	 * @return the forecastTime
	 */
	public String getForecastTime() {
		if (!forecastTime.contains("fcst")) {
			return "000";
		}

		String hours = forecastTime.substring(0, forecastTime.indexOf("hr"));
		
		hours = (hours.length() == 1 ? "00" + hours : (hours.length() == 2 ? "0" + hours : hours)); 
			
		return hours;
	}

	/**
	 * @return the forecastTime
	 */
	public String getRawForecastTime() {
		return this.forecastTime;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((baseTime == null) ? 0 : baseTime.hashCode());
		result = prime * result
				+ ((forecastTime == null) ? 0 : forecastTime.hashCode());
		result = prime * result + ((level == null) ? 0 : level.hashCode());
		result = prime
				* result
				+ ((paramDescription == null) ? 0 : paramDescription.hashCode());
		result = prime * result + ((paramId == null) ? 0 : paramId.hashCode());
		result = prime * result + ((rId == null) ? 0 : rId.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof WgribRecordDescriptor)) {
			return false;
		}
		WgribRecordDescriptor other = (WgribRecordDescriptor) obj;
		if (baseTime == null) {
			if (other.baseTime != null) {
				return false;
			}
		} else if (!baseTime.equals(other.baseTime)) {
			return false;
		}
		if (forecastTime == null) {
			if (other.forecastTime != null) {
				return false;
			}
		} else if (!forecastTime.equals(other.forecastTime)) {
			return false;
		}
		if (level == null) {
			if (other.level != null) {
				return false;
			}
		} else if (!level.equals(other.level)) {
			return false;
		}
		if (paramDescription == null) {
			if (other.paramDescription != null) {
				return false;
			}
		} else if (!paramDescription.equals(other.paramDescription)) {
			return false;
		}
		if (paramId == null) {
			if (other.paramId != null) {
				return false;
			}
		} else if (!paramId.equals(other.paramId)) {
			return false;
		}
		if (rId == null) {
			if (other.rId != null) {
				return false;
			}
		} else if (!rId.equals(other.rId)) {
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("WgribRecordDescriptor [");
		if (baseTime != null)
			builder.append("baseTime=").append(baseTime).append(", ");
		if (forecastTime != null)
			builder.append("forecastTime=").append(forecastTime).append(", ");
		if (level != null)
			builder.append("level=").append(level).append(", ");
		if (paramDescription != null)
			builder.append("paramDescription=").append(paramDescription)
					.append(", ");
		if (paramId != null)
			builder.append("paramId=").append(paramId).append(", ");
		if (rId != null)
			builder.append("rId=").append(rId);
		builder.append("]");
		return builder.toString();
	}
	
}
