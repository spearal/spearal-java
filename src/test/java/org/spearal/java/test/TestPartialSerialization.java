package org.spearal.java.test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.spearal.SpearalFactory;
import org.spearal.SpearalInput;
import org.spearal.configurable.ClassNameAlias;
import org.spearal.impl.ExtendedSpearalOutput;
import org.spearal.partial.PartialObjectProxy;
import org.spearal.partial.UndefinedPropertyException;

public class TestPartialSerialization {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}


//	@Test
//	public void testComplete() throws Exception {
//		int[][] array = new int[20][];
//		
//		if (Object[].class.isAssignableFrom(array.getClass()))
//			System.out.println("##########");
//		
//		System.out.println("--------------------------------------------------------");
//		System.out.println("BooleanBean(...) -> BooleanBean");
//		
//		BooleanBean bean = new BooleanBean(true, true, true);
//		
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		SpearalWriter out = factory.newWriter(baos);
//		out.writeAny(bean);
//		out.flush();
//		
//		byte[] bytes = baos.toByteArray();
//		//System.out.println(toHexString(bytes));
//		
//		Map<String, String> aliases = new HashMap<String, String>();
//		
//		SpearalReader in = factory.newReader(new ByteArrayInputStream(bytes), aliases);
//		Object o = in.readAny();
//
//		System.out.println(o);
//	}

	@Test
	public void testPartialWithAlias() throws Exception {
		System.out.println("--------------------------------------------------------");
		System.out.println("BooleanBean(tall, female) -> ClientBooleanBean");

		SpearalFactory outFactory = new SpearalFactory();

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		BooleanBean bean = new BooleanBean(true, true, true);
		
		ExtendedSpearalOutput out = outFactory.newOutput(baos);
		out.getRequest().addPropertyFilter(BooleanBean.class, "tall", "female");
		out.writeAny(bean);
		
		byte[] bytes = baos.toByteArray();
		System.out.println(toHexString(bytes));

		SpearalFactory inFactory = new SpearalFactory();
		inFactory.getContext().prependConfiguraableItem(new ClassNameAlias(BooleanBean.class, ClientBooleanBean.class));

		SpearalInput in = inFactory.newInput(new ByteArrayInputStream(bytes));
		Object o = in.readAny();

		System.out.println(o);
		if (o instanceof PartialObjectProxy) {
			System.out.println("tall defined: " + ((PartialObjectProxy)o).spearalIsDefined("tall"));
			System.out.println("old defined: " + ((PartialObjectProxy)o).spearalIsDefined("old"));
			System.out.println("female defined: " + ((PartialObjectProxy)o).spearalIsDefined("female"));
			System.out.println("defined properties: " + ((PartialObjectProxy)o).spearalGetDefinedProperties());
		}
		
		ClientBooleanBean b = (ClientBooleanBean)o;
		System.out.println("tall: " + b.isTall());
		System.out.println("female: " + b.isFemale());
		try {
			b.isOld();
			Assert.fail("Should throw an undefined exception");
		}
		catch (UndefinedPropertyException e) {
			System.out.println(e);
		}
	}

//	@Test
//	public void testPartialWithoutAlias() throws Exception {
//		System.out.println("--------------------------------------------------------");
//		System.out.println("BooleanBean(tall, female) -> BooleanBean");
//		
//		BooleanBean bean = new BooleanBean(true, true, true);
//		
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		SpearalWriter out = factory.newWriter(baos);
//		out.getContext().setSelectedProperties(BooleanBean.class, "tall", "female");
//		out.writeAny(bean);
//		out.flush();
//		
//		byte[] bytes = baos.toByteArray();
//		//System.out.println(toHexString(bytes));
//		
//		Map<String, String> aliases = new HashMap<String, String>();
//		
//		SpearalReader in = factory.newReader(new ByteArrayInputStream(bytes), aliases);
//		Object o = in.readAny();
//
//		System.out.println(o);
//	}
//	
//	@Test
//	public void testEvolvedWithAlias() throws Exception {
//		System.out.println("--------------------------------------------------------");
//		System.out.println("BooleanBeanEvolved(...) -> ClientBooleanBean");
//
//		BooleanBeanEvolved bean = new BooleanBeanEvolved(true, true, true, true);
//		
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		SpearalWriter out = factory.newWriter(baos);
//		out.getContext().setSelectedProperties(BooleanBean.class, "tall", "female");
//		out.writeAny(bean);
//		out.flush();
//		
//		byte[] bytes = baos.toByteArray();
//		//System.out.println(toHexString(bytes));
//		
//		Map<String, String> aliases = new HashMap<String, String>();
//		aliases.put(BooleanBeanEvolved.class.getName(), ClientBooleanBean.class.getName());
//		
//		SpearalReader in = factory.newReader(new ByteArrayInputStream(bytes), aliases);
//		Object o = in.readAny();
//
//		System.out.println(o);
//	}
//	
//	@Test
//	public void testCollection() throws Exception {
//		System.out.println("--------------------------------------------------------");
//		System.out.println("Collection<BooleanBean>(0) -> Collection<ClientBooleanBean>(0)");
//		
//		List<BooleanBean> beans = new ArrayList<BooleanBean>();
//		
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		SpearalWriter out = factory.newWriter(baos);
//		//out.getContext().setSelectedProperties(BooleanBean.class, new HashSet<String>(Arrays.asList("tall", "female")));
//		out.writeAny(beans);
//		out.flush();
//		
//		byte[] bytes = baos.toByteArray();
//		//System.out.println(toHexString(bytes));
//		
//		Map<String, String> aliases = new HashMap<String, String>();
//		aliases.put(BooleanBean.class.getName(), ClientBooleanBean.class.getName());
//		
//		SpearalReader in = factory.newReader(new ByteArrayInputStream(bytes), aliases);
//		Object o = in.readAny();
//
//		System.out.println(o);
//	}
//	
//	@Test
//	public void testCollection1() throws Exception {
//		System.out.println("--------------------------------------------------------");
//		System.out.println("Collection<BooleanBean>(1) -> Collection<ClientBooleanBean>(1)");
//
//		List<BooleanBean> beans = new ArrayList<BooleanBean>();
//		beans.add(new BooleanBean(true, true, true));
//		
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		SpearalWriter out = factory.newWriter(baos);
//		//out.getContext().setSelectedProperties(BooleanBean.class, new HashSet<String>(Arrays.asList("tall", "female")));
//		out.writeAny(beans);
//		out.flush();
//		
//		byte[] bytes = baos.toByteArray();
//		//System.out.println(toHexString(bytes));
//		
//		Map<String, String> aliases = new HashMap<String, String>();
//		aliases.put(BooleanBean.class.getName(), ClientBooleanBean.class.getName());
//		
//		SpearalReader in = factory.newReader(new ByteArrayInputStream(bytes), aliases);
//		Object o = in.readAny();
//
//		System.out.println(o);
//	}
//	
//	@Test
//	public void testCollection2() throws Exception {
//		System.out.println("--------------------------------------------------------");
//		System.out.println("Collection<BooleanBean>(1) -> Collection<ClientBooleanBean>(1)");
//
//		List<BooleanBean> beans = new ArrayList<BooleanBean>();
//		beans.add(new BooleanBean(true, true, true));
//		beans.add(beans.get(0));
//		
//		ByteArrayOutputStream baos = new ByteArrayOutputStream();
//		SpearalWriter out = factory.newWriter(baos);
//		out.getContext().setSelectedProperties(BooleanBean.class, "tall", "female");
//		out.writeAny(beans);
//		out.flush();
//		
//		byte[] bytes = baos.toByteArray();
//		//System.out.println(toHexString(bytes));
//		
//		Map<String, String> aliases = new HashMap<String, String>();
//		aliases.put(BooleanBean.class.getName(), ClientBooleanBean.class.getName());
//		
//		SpearalReader in = factory.newReader(new ByteArrayInputStream(bytes), aliases);
//		Object o = in.readAny();
//
//		System.out.println(o);
//		
//		System.out.println("...................");
//		if (o instanceof PartialObjectProxy) {
//			System.out.println("tall defined: " + ((PartialObjectProxy)o).spearalIsDefined("tall"));
//			System.out.println("old defined: " + ((PartialObjectProxy)o).spearalIsDefined("old"));
//			System.out.println("female defined: " + ((PartialObjectProxy)o).spearalIsDefined("female"));
//		}
//		@SuppressWarnings("unchecked")
//		Collection<ClientBooleanBean> c = (Collection<ClientBooleanBean>)o;
//		Assert.assertEquals(beans.size(), c.size());
//	}
//	
////	@Test
////	public void testCollection3() throws Exception {
////		System.out.println("--------------------------------------------------------");
////		System.out.println("Collection<Integer>(1) -> Collection<Integer>(1)");
////		
////		List<Integer> ints = new ArrayList<Integer>();
////		ints.add(0);
////		ints.add((int)Short.MAX_VALUE);
////		ints.add(Integer.MAX_VALUE);
////		
////		ByteArrayOutputStream baos = new ByteArrayOutputStream();
////		SpearalWriter out = factory.newWriter(baos);
////		out.writeAny(ints);
////		out.flush();
////		
////		byte[] bytes = baos.toByteArray();
////		
////		SpearalReader in = factory.newReader(new ByteArrayInputStream(bytes));
////		Object o = in.readAny(new T<ArrayList<Integer>>(){}.getGenericType());
////		
////		System.out.println(o);
////	}
//	
//	public static abstract class T<G> {
//		public final Type getGenericType() {
//			return ((ParameterizedType)getClass().getGenericSuperclass()).getActualTypeArguments()[0];
//		}
//	}

	public static String toHexString(byte[] bytes) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for (int i = 0; i < bytes.length; i++) {
			if (i > 0)
				sb.append(", ");
			sb.append(String.format("0x%02X", Integer.valueOf(bytes[i] & 0xFF)));
		}
		sb.append("]");
		return sb.toString();
	}
}
