package fmtoqn;

public class RouterForOpt extends Router {
	/**
	 * It is the element associated to the optional feature.
	 */
	private QNelement mainChild;

	public QNelement getMainChild() {
		return mainChild;
	}

	public RouterForOpt(String name, QNelement father) {
		super(name, father);
	}

	@Override
	public void addChild(QNelement child) {
		assert !children.contains(child);
		if (!children.contains(child)) {
			if (children.size() > 1) {
				System.err.println("Children of " + this.getName());
				for(QNelement c: children) {
					System.err.println(c.getName());
				}
				throw new Error("A router for an optional feature can have only two children!\nchildren = " + children
						+ "\nchild = " + child.getName());
			}
			super.addChild(child);
		}
	}

	public void setMainChild(QNelement mainChild) {
		if (!children.contains(mainChild)) {
			throw new Error(
					"The element is not one of the children!\nchildren = " + children + "\nmainChild = " + mainChild);
		}
		this.mainChild = mainChild;
	}
}
