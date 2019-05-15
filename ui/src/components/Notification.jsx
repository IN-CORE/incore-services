import React, {Component} from "react";
import {Dialog} from "material-ui";
import config from "../app.config";

class Notification extends Component {

	constructor(props) {
		super(props);
	}

	render() {
		return (
			<Dialog title="Authentication Failed" open={true}>
				<div>
					<h4> Your tokens are either missing or expired. Please do the following steps:</h4>
					<ol>
						<li>Go back to IN-CORE Lab and use the <b>INCORE login</b> found in the Launcher tab.</li>
						<li>After authenticating, use the <b>INCORE apps</b> drop
							down menu to select an application again.
						</li>
						<li>Please make sure you close this tab.</li>
					</ol>
				</div>
			</Dialog>
		);
	}
}

export default Notification;
