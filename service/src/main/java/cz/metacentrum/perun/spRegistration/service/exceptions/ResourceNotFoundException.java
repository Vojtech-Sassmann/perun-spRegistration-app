package cz.metacentrum.perun.spRegistration.service.exceptions;

/**
 * @author Vojtech Sassmann <vojtech.sassmann@gmail.com>
 */
public class ResourceNotFoundException extends Exception {

	public ResourceNotFoundException() {
	}

	public ResourceNotFoundException(String s) {
		super(s);
	}

	public ResourceNotFoundException(String s, Throwable throwable) {
		super(s, throwable);
	}

	public ResourceNotFoundException(Throwable throwable) {
		super(throwable);
	}

	public ResourceNotFoundException(String s, Throwable throwable, boolean b, boolean b1) {
		super(s, throwable, b, b1);
	}
}
