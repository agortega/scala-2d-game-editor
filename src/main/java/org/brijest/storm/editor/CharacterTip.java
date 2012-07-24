package org.brijest.storm.editor;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

public class CharacterTip extends Shell {
	public CLabel imageLabel;

	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			Display display = Display.getDefault();
			CharacterTip shell = new CharacterTip(display);
			shell.open();
			shell.layout();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the shell.
	 * @param display
	 */
	public CharacterTip(Display display) {
		super(display, SWT.CLOSE | SWT.MIN | SWT.MAX | SWT.RESIZE | SWT.ON_TOP | SWT.TOOL);
		setMinimumSize(new Point(128, 64));
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.verticalSpacing = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginHeight = 0;
		gridLayout.horizontalSpacing = 0;
		setLayout(gridLayout);
		
		imageLabel = new CLabel(this, SWT.NONE);
		imageLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		imageLabel.setText("");
		createContents();
	}

	/**
	 * Create contents of the shell.
	 */
	protected void createContents() {
		setText("Character info");
		setSize(128, 128);
		
		Point loc = Display.getCurrent().getCursorLocation();
		this.setLocation(loc.x, loc.y);
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
