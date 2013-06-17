package it.geosolutions.geobatch.annotations;

import java.lang.reflect.Method;

public abstract class AbstractActionServicePostProcessor {

	public AbstractActionServicePostProcessor() {
		super();
	}

	/**
	 * Returns true if annotation is present on the bean
	 * @param annotation
	 * @param bean
	 * @return
	 */
	protected boolean isAnnotationPresent(Class clazz, Object bean){

		//check all the methods of bean
		Method[] methods = bean.getClass().getMethods();

		for (Method method : methods)
			if(!isAnnotationPresent(method, clazz))
				continue;
			else
				return true;

		return false;
	}

	protected boolean isAnnotationPresent(Method method, Class clazz){

		return method.getAnnotation(clazz) != null ? true: false;
	}

	protected void justChecking(){

		isAnnotationPresent(Action.class, null);
	}

}
