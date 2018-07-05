package fmtoqn;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Implements a reflective visitor, see <i>Object invokeMethod(Object argument,
 * String methodName)</i>.
 *
 * @param <T>
 *            the generic type of the type returned by the visit
 */
public abstract class ReflectiveVisitor<T> {

	/**
	 * Equivalent to invokeMethod(object, "visit").
	 * 
	 */
	public T visit(Object object) {
		return invokeMethod(object);
	}

	/**
	 * Equivalent to invokeMethod(object, "visit").
	 */
	protected T invokeMethod(Object object) {
		return invokeMethod(object, "visit");
	}

	/**
	 * Invokes the method (belonging to this class or to a subclass) with the given
	 * name and with one formal parameter whose type is the first interface (i.e.
	 * the first returned by getInterfaces()) implemented by the given argument.
	 * 
	 * @param argument
	 *            the actual parameter
	 * @param methodName
	 *            the method name
	 * @return the object returned by the invoked method
	 */
	protected T invokeMethod(Object argument, String methodName) {
		Method m = null;
		T result = null;
		Class<?>[] interfaces = argument.getClass().getInterfaces();
		Class<?> anInterface = null;
		try {
			// take the first interface
			anInterface = interfaces[0];
			// logger.debug("<visiting>Interface:" + anInterface.getName() + "</visiting>");
			m = this.getClass().getMethod(methodName, anInterface);
			result = (T) m.invoke(this, argument);
		} catch (SecurityException e) {
			throw e;
		} catch (IllegalArgumentException e) {
			throw e;
		} catch (NoSuchMethodException e) {
			throw new RuntimeException("Unknown function " + this + " " + methodName + "(" + anInterface + ")");
			// "Unknown function " + this + " visit(" + anInterface + ")");
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause();
			if (cause instanceof RuntimeException) {
				throw (RuntimeException) e.getCause();
			} else if (cause instanceof java.lang.AssertionError) {
				throw (java.lang.AssertionError) e.getCause();
			} else {
				throw new RuntimeException(e.getCause());
			}
		}
		return result;
	}
}
