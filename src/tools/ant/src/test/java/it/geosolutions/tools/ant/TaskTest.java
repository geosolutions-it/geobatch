/*
 * Copyright (C) 2011 - 2012  GeoSolutions S.A.S.
 * http://www.geo-solutions.it
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */
package it.geosolutions.tools.ant;

import org.apache.tools.ant.Location;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.types.Environment.Variable;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

public class TaskTest {

	private static boolean LINUX;
	private static boolean WINDOWS;

	@BeforeClass
	public static void setUp() throws Exception {
		String os = System.getProperty("os.name").toLowerCase();
		LINUX=os.indexOf("nix") >= 0 || os.indexOf("nux") >= 0;
		WINDOWS=os.indexOf("win") >= 0;
	}

	
	@Test
	public void taskTest(){
		if(LINUX){
			Variable var=new Variable();
			var.setKey("~");
			var.setValue("/");
			try {
				
	//			tg.setName("exec");
				final Project p = new Project();
				p.setBasedir(".");
				Target tg=new Target();
				tg.setName("exec");
				tg.setProject(p);
				tg.setLocation(new Location("."));
				tg.setDepends("");
				
				ExecTask t=Task.buildTask("echo", null, new String[]{"~"}, new Variable[]{var});
	//			t.setProject(p);
				
	//			t.setFailonerror(false);
	//			final String test="output";
	//			t.setOutputproperty(test);
	//			tg.addTask(t);
				
				Task.addToTarget(tg, t);
				
				Task.addToProject(p, tg);
	//			p.addTarget(tg);
	//			p.init();
				t.execute();
				tg.execute();
				p.executeTarget(tg.getName());
				
	//			System.out.print("oputput: "+t.getProject().getProperty(test));
				
				
	//			t.setFailonerror(false);
	//			t.setOutputproperty("TEST");
	//			t.execute();
	//			Task.buildTask("echo", null, new String[]{"hello","world"}, new Variable[]{var}).execute();

				
			} catch (Exception e){
				e.printStackTrace();
				Assert.fail(e.getLocalizedMessage());
				
			}
		} else if(WINDOWS){
			
		}
	}
	
	@Test
	public void taskNewTest(){
		if(LINUX){
			Variable var=new Variable();
			var.setKey("VAR");
			var.setValue("VAR_VALUE");
			try {
				
	//			tg.setName("exec");
				final Project p = new Project();
				p.init();
				
				Target tg=new Target();
				tg.setName("echo");
				tg.setProject(p);
				
				ExecTask t=Task.buildTask("echo", null, new String[]{"-> ${VAR}"}, new Variable[]{var});
	//			t.setFailonerror(false);
	//			final String test="output";
	//			t.setOutputproperty(test);
				
				Task.addToTarget(tg, t);
				
				Task.addToProject(p, tg);
				
				t.execute();
				tg.execute();
				p.executeTarget(tg.getName());
				
	//			System.out.print("oputput: "+t.getProject().getProperty(test));
				
	//			t.setFailonerror(false);
	//			t.setOutputproperty("TEST");
	//			t.execute();
	//			Task.buildTask("echo", null, new String[]{"hello","world"}, new Variable[]{var}).execute();
				
			} catch (Exception e){
				e.printStackTrace();
				Assert.fail(e.getLocalizedMessage());
				
			}

		} else if(WINDOWS){
			
		}					
	}
	
	@Test
	public void taskEchoTest(){
		if(LINUX){
			Variable var=new Variable();
			var.setKey("~");
			var.setValue("/");
			try {
				Project p;
				String targetName="NAME";
				p=Task.buildSimpleProject(targetName,"echo");
				p.executeTarget(targetName);
							
			} catch (Exception e){
				e.printStackTrace();
				Assert.fail(e.getLocalizedMessage());
				
			}

		} else if(WINDOWS){
			
		}				
	}

}
