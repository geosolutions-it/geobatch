Dynamic Tokens
==============


The dynamic tokens library has been developed in order to allow runtime flexible configuration of variable values. Regular expressions are used to extract substrings.

Sample configuration file
-------------------------

Let's say you need to create a date string starting from a file name of a known form.
The implementation code does not know in advance how this date is formatted, and you leave the extrapolation in the config file.

This is a sample file name: `REP12_20120809_20120811_RFVL.nc.gz`.  
The date you need is the first one ('20120809').  

This is the configuration XML:

.. sourcecode:: xml

 <dynamicTokens>
  <stringToken>
   <name>runtime</name>
    <base>${FILENAME}</base>
    <regex>.*_([0-9]{4})([0-9]{2})([0-9]{2})_[0-9]{8}_.*</regex>
    <compose>${1}-${2}-${3} 00:00:00</compose>
  </stringToken>
 </dynamicTokens>

It says
* create a token with _name_ `runtime`
* use a given _base_ string from where we'll extract substrings from
* extract the capturing groups using the provided _regular expression_
* and put the first, second, and third captured groups in the final _composite_ string

You see in this example that the given base is itself defined using a _token_. 

Tokens are in the form `${tokenname}`. They can be used both in the `base` and in the `compose` elements.  
A token name must be defined before its use.  
You may have 3 kinds of token:
* known values token -- they are provided programmatically
* dynamic tokens -- the ones you are defining (e.g. `runtime`)
* numeric tokens -- they are only used in the `compose` element; the number represents the capturing group number expressed in the related regular expression.


You have to feed some known values to the dynamic tokens resolver in order to have it process its data.

You feed the know data programmatically; in this example we are feeding the token name `FILENAME`:

.. sourcecode:: xml

  DynTokenList tokenList = ...unmarshall the XML configuration...
  DynTokenResolver tokenResolver = new DynTokenResolver(tokenList);
  tokenResolver.setBaseToken("FILENAME", filename);
  tokenResolver.resolve();
  String tokenRuntime = tokenResolver.getResolvedTokens().get("runtime");

From the previous code (and configuration), you'll get the string `2012-08-09 00:00:00`.

Note that you can define multiple tokens; once a token has been resolved, you can use it in next tokens definition.

Note that tokens are expanded both in `base` and `compose` elements.
Here another example:

.. sourcecode:: xml

  <dynamicTokens>
    <stringToken>
      <name>year</name>
      <base>${FILENAME}</base>
      <regex>.*_([0-9]{4})[0-9]{2}[0-9]{2}_[0-9]{8}_.*</regex>
      <compose>${1}</compose>
    </stringToken>
    <stringToken>
      <name>month</name>
      <base>${FILENAME}</base>
      <regex>.*_[0-9]{4}([0-9]{2})[0-9]{2}_[0-9]{8}_.*</regex>
      <compose>${1}</compose>
    </stringToken>
    <stringToken>
      <name>day</name>
      <base>${FILENAME}</base>
      <regex>.*_[0-9]{4}[0-9]{2}([0-9]{2})_[0-9]{8}_.*</regex>
      <compose>${1}</compose>
    </stringToken>
    <stringToken>
      <name>runtime</name>
      <base>${FILENAME}</base>
      <regex>.*_([0-9]{4})([0-9]{2})([0-9]{2})_[0-9]{8}_.*</regex>
      <compose>${year}-${month}-${day} 00:00:00</compose>
    </stringToken>
  </dynamicTokens>

This configuration will set the `runtime` token with the same value it had in the previous example; anyway here we are defining three more tokens (`year`,`month`,`day`) that can be used both as input for next token definition (e.g. `<compose>${year}-${month}-${day} 00:00:00</compose>`), and can be retrieved programmatically; e.g.

.. sourcecode:: xml

  String y = tokenResolver.getResolvedTokens().get("year");
  String m = tokenResolver.getResolvedTokens().get("month");
