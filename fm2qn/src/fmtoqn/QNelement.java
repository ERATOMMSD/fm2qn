package fmtoqn;

import java.util.ArrayList;
import java.util.List;

public abstract class QNelement {
	private String name;
	private QNelement father;
	protected List<QNelement> children;

	public QNelement(String name, QNelement father) {
		this.name = name;
		this.father = father;
		children = new ArrayList<QNelement>();
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public QNelement getFather() {
		return father;
	}

	public void addChild(QNelement child) {
		for (QNelement c : children) {
			if (c.getName().equals(child.getName())) {
				//throw new Error("Child " + c.getName() + " already added to " + name);
				 return;
			}
		}
		children.add(child);
	}

	public List<QNelement> getChildren() {
		return children;
	}

	public void removeChild(QNelement child) {
		children.remove(child);
	}

	public void setFather(QNelement father) {
		this.father = father;
	}

	public abstract String toXml();

	public abstract String connectionsToXml();
}
