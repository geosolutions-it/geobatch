Octave Service
==============

This module add the Octave support to the GeoBatch platform.

Octave Environment
------------------------------------------------------------

The octave environment is composed by an ordered sequence of sheets, it represents the complete execution flow of an Octave process and it's executed atomically.

.. sourcecode:: xml

	<!-- OCTAVE ENV -->
    <octave>
   	 <sheets>
   		 <sheet name="A">
   			 ...
   		 </sheet>

   		 ...

   		 <sheet name="Z">
   			 ...
   		 </sheet>
   	 </sheets>
    </octave>

Octave Executable Sheet
------------------------------------------------------------

Each sheet is defined by its name (which should be unique within the same environment).
A sheet contains:

 '''commands''': (alternative*)
 
   List of octave simple commands to run

 '''functions''': (alternative*)
 
   List of functions which should be executed by the Octave process. this could be as complex as you want but you'll have to provide a preprocessor to execute it.

   The function part of a sheet is essentially a sheet itself but it is not an executable sheet.

   The ExecutableSheet is different from a simple Sheet since it never contain a function node (which need to be preprocessed to become executable).

   '''definitions''': (optional)

   List of variables which should be passed to the Octave process from the Java environment (this happen before the first command is executed)

   '''returns''': (optional)

   List of variables which should be passed from the Octave process to the Java environment (this happen after the last command is executed)

* alternative -> at last one of these should be present

.. sourcecode:: xml

	<!-- OCTAVE ENV -->
    <octave>
      <sheets>
    	<!-- OCTAVE SHEET -->
    	<sheet name="MARS3DFlow">
      	<commands>
        	<OctaveCommand executed="false">
   		 <command>source "/usr/share/octave/3.0.5/m/startup/octaverc";</command>
        	</OctaveCommand>
        	<OctaveCommand executed="false">
   		 <command>cd "/home/user/work/data/rep10workingdir/"</command>
        	</OctaveCommand>
      	</commands>
      	<definitions/>
      	<functions>
        	<function name="function">
   		 <commands/>
   		 <returns/>
   		 <definitions>
   			 <OctaveFile name="file_in" value="" output="false" input="true"/>
   			 <OctaveFile name="file_out" value="" output="true" input="false"/>
   		 </definitions>
        	</function>
      	</functions>
      	<returns/>
    	</sheet>
      </sheets>
    </octave>


Preprocessor
-----------------------------------------------------------------

'''Why we need preprocessors?'''
 Looking ahead to the example we will try to preprocess a function which should be called using 2 variables (which should be passed from the Java to the Octave environment).

 Those variables, (we assume), are not defined until the program execution starts.

This sheet need to be preprocessed since it contains a function:

.. sourcecode:: xml

 	<function name="function">
    <commands/>
    <returns/>
    <definitions>
   	 <OctaveFile name="file_in" value="" output="false" input="true"/>
   	 <OctaveFile name="file_out" value="" output="true" input="false"/>
    </definitions>
 	</function>

This sheet is a (not so good) preprocessed version of the above one...

.. sourcecode:: xml

 	</commands>
   	<OctaveCommand executed="false">
      <command>function(file_in,file_out);</command>
   	</OctaveCommand>
 	</commands>
 	<definitions>
    <OctaveFile name="file_in" value="?" output="false" input="true"/>
    <OctaveFile name="file_out" value="?" output="true" input="false"/>
 	</definitions>


Looking at the OctaveFile variables we can still see that their value '?' are undefined!

''So the runtime preprocessing is needed.''

'''What a preprocessor should do?'''
   Transform a Sheet in an ExecutableSheet initializing all the needed input (definitions) and output (returns) variables and building the command string which call that function.

'''NOTE''':
   The function prototype is -> [OUT1, OUT2, ... ,OUTn] = function(IN1, IN2, ..., INn);

   Refer to the Octave/Matlab documentation for detailed documentation.

