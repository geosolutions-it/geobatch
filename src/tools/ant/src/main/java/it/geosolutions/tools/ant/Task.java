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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.ExecTask;
import org.apache.tools.ant.types.Environment;
import org.apache.tools.ant.types.Environment.Variable;

public class Task {

    public static void updateEnv(final File props, Environment env) throws IOException {
        InputStream is = null;
        InputStream bis = null;
        try {
            is = new FileInputStream(props);
            bis=new BufferedInputStream(is);
            final Properties prop = new Properties();
            prop.load(bis);
            final Set<Entry<Object, Object>> set = prop.entrySet();
            final Iterator<Entry<Object, Object>> it = set.iterator();
            while (it.hasNext()) {
                final Entry e = it.next();
                final Variable v = new Variable();
                v.setKey(e.getKey().toString());
                v.setValue(e.getValue().toString());
                env.addVariable(v);
            }
            // } catch (IOException e){
            // throw e;
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {

                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {

                }
            }
        }
    }

    public static Variable[] loadVars(final File props) throws IOException {
        InputStream is = null;
        InputStream bis = null;
        Variable[] vars;
        try {
            is = new FileInputStream(props);
            bis=new BufferedInputStream(is);
            final Properties prop = new Properties();
            prop.load(is);
            final Set<Entry<Object, Object>> set = prop.entrySet();
            vars = new Variable[set.size()];
            int i = 0;
            final Iterator<Entry<Object, Object>> it = set.iterator();
            while (it.hasNext()) {
                final Entry e = it.next();
                final Variable v = new Variable();
                v.setKey(e.getKey().toString());
                v.setValue(e.getValue().toString());
                vars[i++] = v;
            }
            // } catch (IOException e){
            // throw e;
        } finally {
            if (bis != null) {
                try {
                    bis.close();
                } catch (IOException e) {

                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {

                }
            }
        }
        return vars;
    }

    public static Project buildSimpleProject(String targetName, String... executables)
        throws IllegalArgumentException {
        final Project project = new Project();
        Target target = new Target();
        target.setName(targetName);
        target.setProject(project);
        addToTarget(target, buildTask(executables));
        project.addTarget(target);
        project.setDefault(target.getName());
        project.init();
        return project;
    }

    public static Project buildSimpleProject(String targetName, org.apache.tools.ant.Task... tasks)
        throws IllegalArgumentException {
        final Project project = new Project();
        Target target = new Target();
        target.setName(targetName);
        target.setProject(project);
        addToTarget(target, tasks);
        project.addTarget(target);
        project.setDefault(target.getName());
        project.init();
        return project;
    }

    public static Project buildProject(final Target... targets) throws IllegalArgumentException {
        final Project project = new Project();
        addToProject(project, targets);
        return project;
    }

    /**
     * connect targets to the project
     * 
     * @param project
     * @param updateChild connect tasks as child of target and targets as child
     *            of project
     * @param updateParent update tasks parent project to the target parent one
     * @param targets
     * @return
     */
    public static int connectTargets(final Project project, final boolean updateChild,
                                     final boolean updateParent, final Target... targets) {
        int numTarget = 0;
        for (Target target : targets) {
            if (target != null) {
                numTarget++;
                if (updateChild) {
                    project.addTarget(target.getName(), target);
                }
                if (updateParent) {
                    target.setProject(project);
                    // target are already connected to their children
                    if (connectTasks(target, false, updateParent, target.getTasks()) == 0) {
                        // if no tasks were updated
                        numTarget--;
                    }
                }
            }
        }
        return numTarget;
    }

    /**
     * connect tasks to the target and/or to the parent project. Be sure that
     * the target is already connected to the Project.
     * 
     * @param target
     * @param updateChild connect tasks as child of target
     * @param updateParent update tasks parent project to the target parent one
     * @param tasks list of tasks to update
     * @return the number of tasks updated
     */
    public static int connectTasks(final Target target, final boolean updateChild,
                                   final boolean updateParent, final org.apache.tools.ant.Task... tasks)
        throws IllegalArgumentException {
        int numTask = 0;
        for (org.apache.tools.ant.Task task : tasks) {
            if (task != null) {
                numTask++;
                if (updateParent) {
                    if (target.getProject() == null) {
                        throw new IllegalArgumentException(
                                                           "Unable to add tasks \'"
                                                               + task.getTaskName()
                                                               + "\' to the target named \'"
                                                               + target.getName()
                                                               + "\'with not initialized project, use target.setProject(...) before this call");
                    }
                    task.setProject(target.getProject());
                }
                if (updateChild) {
                    target.addTask(task);
                }
            }
        }
        return numTask;
    }

    public static void addToProject(final Project project, final Target... targets)
        throws IllegalArgumentException {
        if (project == null) {
            throw new IllegalArgumentException("Unable to add tasks to a null project");
        }
        if (targets == null || targets.length == 0) {
            throw new IllegalArgumentException(
                                               "Unable to build a project using a null or empty list of targets");
        }

        int numTarget = connectTargets(project, true, true, targets);

        if (numTarget == 0) {
            throw new IllegalArgumentException(
                                               "Unable to build a project using this list of targets (empty target or epty task list)");
        }
        // initialize the project
        project.init();
    }

    public static void addToTarget(final Target target, final org.apache.tools.ant.Task... tasks)
        throws IllegalArgumentException {
        if (target == null) {
            throw new IllegalArgumentException("Unable to add tasks to a null target");
        } else if (target.getProject() == null) {
            throw new IllegalArgumentException(
                                               "Unable to add tasks to a target with not initialized project, "
                                                   + "use target.setProject(...) before this call");
        }
        if (tasks == null || tasks.length == 0) {
            throw new IllegalArgumentException("Unable to add tasks using a null or empty list of tasks");
        }

        int numTask = connectTasks(target, true, true, tasks);

        if (numTask == 0) {
            throw new IllegalArgumentException("Unable to build a project using a list of null targets");
        }
    }

    /**
     * builds an ExecTask which could be executed or linked into a project.
     * 
     * @param executable
     * @param workingDir
     * @param args
     * @param envVars
     * @return
     * @throws IllegalArgumentException
     */
    public static ExecTask buildTask(final String executable, final File workingDir, final String[] args,
                                     final Variable[] envVars) throws IllegalArgumentException {

        final ExecTask task = new ExecTask();

        if (executable == null || executable.isEmpty()) {
            throw new IllegalArgumentException("Unable to create a task without the command string");
        }
        task.setExecutable(executable);

        if (envVars != null) {
            for (Variable var : envVars) {
                if (var != null) {
                    task.addEnv(var);
                }
            }
        }
        if (args != null) {
            for (String arg : args) {
                if (arg != null && !arg.isEmpty()) {
                    task.createArg().setValue(arg);
                }
            }
        }

        // use workingDir, if null use current path
        task.setDir((workingDir != null) ? workingDir : new File("."));

        return task;
    }

    public static ExecTask[] buildTask(String... executables) throws IllegalArgumentException {
        if (executables == null || executables.length == 0) {
            throw new IllegalArgumentException("Unable to add tasks using a null or empty list of tasks");
        }
        int numTask = 0;
        List<ExecTask> list = new ArrayList<ExecTask>();
        for (String exec : executables) {
            if (exec != null && !exec.isEmpty()) {
                numTask++;
                list.add(buildTask(exec, null));
            }
        }
        if (numTask == 0) {
            throw new IllegalArgumentException("Unable to build a project using a list of null targets");
        }
        return list.toArray(new ExecTask[] {});
    }

    public static ExecTask buildTask(String executable, String[] args) throws IllegalArgumentException {
        return Task.buildTask(executable, null, args, null);
    }

}
