==================================================
How to add aliases for your beans
================================================== by ETj

In the module where the classes to be aliased are defined, perform the
following steps:

==================================================
Modify the module's pom.xml, adding this dep:

    <dependency>
        <groupId>it.geosolutions</groupId>
        <artifactId>gb-registry</artifactId>
    </dependency>

==================================================
Add a registrar class where the alias are defined:

package it.geosolutions.geobatch.XXXXXXX

import it.geosolutions.geobatch.registry.AliasRegistrar;
import it.geosolutions.geobatch.registry.AliasRegistry;

public class XXXXAliasRegistrar implements AliasRegistrar {

     public XXXXAliasRegistrar(AliasRegistry registry) {
         System.out.println("Registering alias for XXXX module");
         registry.putAlias("your xstream tag",
                    it.geosolutions.geobatch.XXXX.YourMappedClassHere.class);
     }
 }

==================================================
Add your registrar to the applicationContext

    <bean id="xxxxRegistrar" class="it.geosolutions.geobatch.xxxx.XXXXRegistrar" lazy-init="false">
        <constructor-arg ref="aliasRegistry" />
    </bean>

If you have other beans in your appcontext that requires the loading of
services, make sure the Registrar is instantiated before them, adding
depends-on where needed.

