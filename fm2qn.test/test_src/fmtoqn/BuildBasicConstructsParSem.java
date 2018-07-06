package fmtoqn;

import org.junit.Test;

import fmtoqn.builder.BuildQNfromFM;

public class BuildBasicConstructsParSem {

	public void build(String name) throws Exception {
		BuildQNfromFM.parSemantics = true;
		BuildQNfromFM b = new BuildQNfromFM("./basicConstructs/" + name + ".afm");
		b.buildQN();
		b.saveQn("./basicConstructs/" + name + "ParSem.jsimg");
	}

	@Test
	public void buildOptional() throws Exception {
		build("optional");
	}

	@Test
	public void buildMandatory() throws Exception {
		build("mandatory");
	}

	@Test
	public void buildOr() throws Exception {
		build("or");
	}

	@Test
	public void buildOr2() throws Exception {
		build("or2");
	}

	@Test
	public void buildOr3() throws Exception {
		build("or3");
	}

	@Test
	public void buildOrAlt() throws Exception {
		build("orAlt");
	}

	@Test
	public void buildOrAlt2() throws Exception {
		build("orAlt2");
	}

	@Test
	public void buildOrOpt() throws Exception {
		build("orOpt");
	}

	@Test
	public void buildOrOpt2() throws Exception {
		build("orOpt2");
	}

	@Test
	public void buildOrOpt3() throws Exception {
		build("orOpt3");
	}

	@Test
	public void buildAlternative() throws Exception {
		build("alternative");
	}

	@Test
	public void buildAnd() throws Exception {
		build("and");
	}

	@Test
	public void buildAnd2() throws Exception {
		build("and2");
	}

	@Test
	public void buildAnd3() throws Exception {
		build("and3");
	}

	@Test
	public void buildAndAlt() throws Exception {
		build("andAlt");
	}

	@Test
	public void buildOptionalAlt() throws Exception {
		build("optionalAlt");
	}
}
