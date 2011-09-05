/*
 *  GeoBatch - Open Source geospatial batch processing system
 *  http://code.google.com/p/geobatch/
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
package it.geosolutions.geobatch.octave;

import java.util.ArrayList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamInclude;
import com.thoughtworks.xstream.annotations.XStreamOmitField;

@XStreamAlias("sheet")
@XStreamInclude({
    OctaveCommand.class,
    SerializableOctaveObject.class,
    ArrayList.class})
public class OctaveExecutableSheet {
    
    // the name of this sheet
    @XStreamAsAttribute
    @XStreamAlias("name")
    private final String name;
    
    @XStreamAlias("commands")
    private final List<OctaveCommand> commands;
    
    // variables defined by this sheet
    @XStreamAlias("definitions")
    private final List<SerializableOctaveObject<?>> definitions;
    
    /**
     * calculated variables:
     * Vector of variables to be checked out from the octave
     * environment after the 'sheet' execution 
     * @param com
     * @param defs
     * @param functs
     */
    @XStreamAlias("returns")
    private final List<SerializableOctaveObject<?>> returns;
    
    @XStreamOmitField
    private boolean executed=false;
    
    @Override
    public Object clone(){
        List<OctaveCommand> comm=new ArrayList<OctaveCommand>();
        if (commands!=null)
            for (OctaveCommand oc:commands){
                comm.add((OctaveCommand)oc.clone());
            }
        List<SerializableOctaveObject<?>> def=new ArrayList<SerializableOctaveObject<?>>();
        if (definitions!=null)
            for (SerializableOctaveObject<?> d:definitions){
                def.add((SerializableOctaveObject<?>)d.clone());
            }
        List<SerializableOctaveObject<?>> ret=new ArrayList<SerializableOctaveObject<?>>();
        if (returns!=null)
            for (SerializableOctaveObject<?> r:returns){
                ret.add((SerializableOctaveObject<?>)r.clone());
            }
        OctaveExecutableSheet oes=new OctaveExecutableSheet(this.getName(),comm,def,ret);
        oes.setExecuted(this.isExecuted());
        return oes;
    }
    
    public OctaveExecutableSheet(
            String sheet_name,
            List<OctaveCommand> com,
            List<SerializableOctaveObject<?>> defs,
            List<SerializableOctaveObject<?>> rets){
        name=sheet_name;
        commands=com;
        definitions=defs;
        returns=rets;
        executed=false;
    }
    
    public OctaveExecutableSheet(
            String sheet_name,
            OctaveCommand com,
            List<SerializableOctaveObject<?>> defs,
            List<SerializableOctaveObject<?>> rets){
        commands=new ArrayList<OctaveCommand>();
        if (sheet_name!=null)
            name=sheet_name;
        else {
// TODO LOG
            name="new_sheet";
        }
        if (com!=null)
            commands.add(com);
        //else
// TODO LOG
        definitions=defs;
        returns=rets;
        executed=false;
    }
    
    public OctaveExecutableSheet(){
        name="new_sheet";
        commands=new ArrayList<OctaveCommand>();
        definitions=new ArrayList<SerializableOctaveObject<?>>();
        returns=new ArrayList<SerializableOctaveObject<?>>();
        executed=false;
    }
    
    public OctaveExecutableSheet(OctaveExecutableSheet es){
        if (es!=null){
            name=es.getName();
            commands=es.getCommands();
            definitions=es.getDefinitions();
            returns=es.getReturns();
            executed=es.isExecuted();            
        }
        else {
// TODO LOG
            name="new_sheet";
            commands=new ArrayList<OctaveCommand>();
            definitions=new ArrayList<SerializableOctaveObject<?>>();
            returns=new ArrayList<SerializableOctaveObject<?>>();
            executed=false;
        }
    }
    
    public String getName(){
        return name;
    }
    
    public OctaveCommand getCommand(int i){
        return commands.get(i);
    }
    
    public List<OctaveCommand> getCommands(){
        return commands;
    }
    
    public List<SerializableOctaveObject<?>> getDefinitions(){
        return definitions;
    }
    
    public List<SerializableOctaveObject<?>> getReturns(){
        return returns;
    }
    
    // schedule command to be executed
    public void pushCommand(OctaveCommand src){
        commands.add(src);
    }
    
    // schedule command to be executed
    public void pushCommand(String src){
        if (src!=null && src!="")
            commands.add(new OctaveCommand(src));
//        else
// TODO LOG
    }
    
    public OctaveCommand popCommand(){
        if (commands.isEmpty())
            return null;
// TODO LOG
        else {
            OctaveCommand c=commands.get(0);
            commands.remove(0);
            return c;
        }
    }
    
    public boolean hasDefinitions(){
        if (definitions.isEmpty())
            return false;
        else
            return true;
    }
    
    /**
     * Check if sheet has SerializableOctaveObject which
     * represents returning values.
     * @return true if sheet has returning variables
     */
    public boolean hasReturns(){
        if (returns.isEmpty())
            return false;
        else
            return true;
    }
    
    /**
     * check if this sheet has commands
     * @note this do not check the executed flag
     * @return true if sheet has commands in the list
     */
    public boolean hasCommands(){
        if (commands.isEmpty())
            return false;
        else
            return true;
    }
    
    /**
     * Check the execution flag of this sheet
     * @return true if all the OctaveFunctions of
     * this sheet are already executed.
     */
    public boolean isExecuted(){
        return executed;
    }
    
    /**
     * set the executed status of this sheet to the
     * input parameter 
     * @param exec
     */
    protected void setExecuted(boolean exec){
        executed=exec;
    }
    
    /**
     * re-check execution flag of this sheet 
     * checking all the contained OctaveCommands
     * execution flags.
     * @note this method also set this.executed
     * flag
     * @return true if all the OctaveFunctions of
     * this sheet are already executed.
     */
    protected boolean checkExecuted(){
        executed=true;
        for (OctaveCommand oc:commands){
            if (!oc.isExecuted()){
                executed=false;
                break;
            }
        }   
        return executed;
    }
    
    /**
     * extract a definition from the list
     * @note this remove a definition
     * @return the SerializableOctaveObject which represent
     * the definition
     */
    public SerializableOctaveObject<?> popDefinition(){
        if (definitions.isEmpty())
            return null;
        else {
            SerializableOctaveObject<?> v=definitions.get(0);
            definitions.remove(0);
            return v;
        }
    }
    
    public SerializableOctaveObject<?> popReturn(){
        if (returns.isEmpty())
            return null;
// TODO LOG
        else {
            SerializableOctaveObject<?> r=returns.get(0);
            returns.remove(0);
            return r;
        }
    }
    
    public void pushDefinition(SerializableOctaveObject<?> d){
        definitions.add(d);
    }
    
    public void pushDefinitions(List<SerializableOctaveObject<?>> ds){
        definitions.addAll(ds);
    }
    
    protected void pushReturn(SerializableOctaveObject<?> r){
        returns.add(r);
    }
    
    public void pushReturns(List<SerializableOctaveObject<?>> rs){
        returns.addAll(rs);
    }
}


