 /*
 * Copyright (c) 2001, Zoltan Farkas All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package com.zoltran.perf.impl;

import java.io.IOException;
import java.util.List;
import java.util.Properties;




public interface RRDMeasurementDatabaseMBean
{

    List<String> generate(Properties props) throws IOException;

    List<String> generateCharts(int width, int height) throws IOException;
    
    List<String> generateCharts(long startTimeMillis, long endTimeMillis, int width, int height) throws IOException;

    List<String> getMeasurements() throws IOException;

    List<String> getParameters();
    
}

