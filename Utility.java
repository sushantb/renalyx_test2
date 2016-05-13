package com.hp.eprint.batchExample;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventLocator;
import javax.xml.bind.util.ValidationEventCollector;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;

import org.xml.sax.SAXException;


public abstract class Utility {

	private static final ConcurrentHashMap<String, JAXBContext> jaxbContextMap = new ConcurrentHashMap<String, JAXBContext>();

	private static final Pattern pattern = Pattern.compile("^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");;

	/**
	 * 
	 * @param contextPath
	 * @return
	 * @throws JAXBException
	 */
	private static JAXBContext getJaxbContext(String contextPath) throws JAXBException {
		JAXBContext jaxbContext = jaxbContextMap.get(contextPath);
		if (jaxbContext == null) {
			synchronized (Utility.class) {
				jaxbContext = jaxbContextMap.get(contextPath);
				if (jaxbContext == null) {
					jaxbContext = JAXBContext.newInstance(contextPath);
					jaxbContextMap.put(contextPath, jaxbContext);
				}
			}
		}
		return jaxbContext;
	}

	private static JAXBContext getJaxbContext(String contextPath, Class clazz) throws JAXBException {
		JAXBContext jaxbContext = jaxbContextMap.get(contextPath);
		if (jaxbContext == null) {
			synchronized (Utility.class) {
				jaxbContext = jaxbContextMap.get(contextPath);
				if (jaxbContext == null) {
					jaxbContext = JAXBContext.newInstance(clazz);
					jaxbContextMap.put(contextPath, jaxbContext);
				}
			}
		}
		return jaxbContext;
	}

	public static <T> String marshal(T object) throws JAXBException {
		String className = object.getClass().getPackage().getName();
		return Utility.marshal(className, object);
	}

	public static <T> Object unmarshal(Class<T> docClass, InputStream inputStream) throws JAXBException {
		String className = docClass.getPackage().getName();
		return Utility.unmarshal(className, docClass, inputStream);
	}

	public static <T> Object unmarshal(String input, Class clazz) throws JAXBException {
		StringReader reader = new StringReader(input);

		Object retValue = null;

		JAXBContext jc = getJaxbContext(clazz.getSimpleName(), clazz);
		if (jc == null)
			throw new JAXBException("JaxbContext null for the context path:" + clazz.getSimpleName());
		Unmarshaller u = jc.createUnmarshaller();
		retValue = u.unmarshal(reader);

		return retValue;

	}

	public static <T> String marshal(Class clazz, Object object) throws JAXBException {
		String retValue = null;

		JAXBContext jc = getJaxbContext(clazz.getSimpleName(), clazz);
		if (jc == null)
			throw new JAXBException("JaxbContext null for the context path:" + clazz.getSimpleName());
		Marshaller u = jc.createMarshaller();
		StringWriter sw = new StringWriter();
		u.marshal(object, sw);
		retValue = sw.toString();

		return retValue;
	}

	public static <T> String marshal(String contextPath, T object) throws JAXBException {
		String retValue = null;

		JAXBContext jc = getJaxbContext(contextPath);
		if (jc == null)
			throw new JAXBException("JaxbContext null for the context path:" + contextPath);
		Marshaller u = jc.createMarshaller();
		StringWriter sw = new StringWriter();
		u.marshal(object, sw);
		retValue = sw.toString();

		return retValue;
	}

	public static <T> String marshal(JAXBContext jaxbContext, T object) throws JAXBException {
		String stringRepresentation = null;
		Marshaller marshaller = null;
		StringWriter stringWriter = null;

		stringWriter = new StringWriter();
		marshaller = jaxbContext.createMarshaller();
		marshaller.marshal(object, stringWriter);
		stringRepresentation = stringWriter.toString();

		return stringRepresentation;
	}

	public static <T> Object unmarshal(String contextPath, Class<T> docClass, InputStream inputStream) throws JAXBException {
		Object retValue = null;

		JAXBContext jc = getJaxbContext(contextPath);
		if (jc == null)
			throw new JAXBException("JaxbContext null for the context path:" + contextPath);
		Unmarshaller u = jc.createUnmarshaller();
		retValue = u.unmarshal(inputStream);

		return retValue;
	}

	public static <T> Object unmarshal(JAXBContext jaxbContext, Class<T> requestType, InputStream inputStream) throws JAXBException, SAXException {

		Object retValue = null;
		Unmarshaller unmarshaller = null;

		// After the Unmarshaller object has been established,
		// you pass it the schema.

		unmarshaller = jaxbContext.createUnmarshaller();

		StreamSource source = new StreamSource(inputStream);
		retValue = unmarshaller.unmarshal(source, requestType).getValue();

		return retValue;

	}

	public static <T> Object unMarshalAndValidate(Class<T> docClass, InputStream inputStream, Schema mySchema) throws JAXBException, SAXException {
		String className = docClass.getPackage().getName();

		Object retValue = Utility.unMarshalAndValidate(className, docClass, inputStream, mySchema);

		return retValue;
	}

	public static <T> Object unMarshalAndValidate(JAXBContext jaxbContext, Class<T> requestType, InputStream inputStream, Schema mySchema) throws JAXBException, SAXException {

		Object retValue = null;
		Unmarshaller unmarshaller = null;

		ValidationEventCollector vec = new ValidationEventCollector();
		try {
			//

			// After the Unmarshaller object has been established,
			// you pass it the schema.

			unmarshaller = jaxbContext.createUnmarshaller();
			unmarshaller.setSchema(mySchema);

			// pass a ValidationEventCollector to the unmarshaller which will
			// store validation events into it so that you can retrieve an
			// event and query its individual attributes

			unmarshaller.setEventHandler(vec);

			StreamSource source = new StreamSource(inputStream);
			retValue = unmarshaller.unmarshal(source, requestType).getValue();

		} finally {
			if (vec.hasEvents()) {
				for (ValidationEvent ve : vec.getEvents()) {
					String msg = ve.getMessage();
					ValidationEventLocator vel = ve.getLocator();
					int line = vel.getLineNumber();
					int column = vel.getColumnNumber();
					
				}
			}

		}

		return retValue;

	}

	public static <T> Object unMarshalAndValidate(String contextPath, Class<T> docClass, InputStream inputStream, Schema mySchema) throws JAXBException, SAXException {
		Object retValue = null;

		ValidationEventCollector vec = new ValidationEventCollector();
		try {
			//

			// After the Unmarshaller object has been established,
			// you pass it the schema.

			JAXBContext jc = getJaxbContext(contextPath);
			if (jc == null)
				throw new JAXBException("JaxbContext null for the context path:" + contextPath);

			Unmarshaller u = jc.createUnmarshaller();
			u.setSchema(mySchema);

			// pass a ValidationEventCollector to the unmarshaller which will
			// store validation events into it so that you can retrieve an
			// event and query its individual attributes

			u.setEventHandler(vec);

			StreamSource source = new StreamSource(inputStream);
			retValue = u.unmarshal(source, docClass).getValue();

		} finally {
			if (vec.hasEvents()) {
				for (ValidationEvent ve : vec.getEvents()) {
					String msg = ve.getMessage();
					ValidationEventLocator vel = ve.getLocator();
					int line = vel.getLineNumber();
					int column = vel.getColumnNumber();

				}
			}

		}

		return retValue;

	}

}
