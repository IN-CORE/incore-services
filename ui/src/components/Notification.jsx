import React, { Component } from "react";
import { Dialog, RaisedButton } from "material-ui";

class Notification extends Component {

	constructor(props) {
		super(props);
		this.state = { show: props.show };
	}

	componentWillReceiveProps(nextProps) {
		this.setState({ show: nextProps.show });
	}

	handleClose = () => {
		this.setState({show: false});
	};

	render() {
		const action = [
			<RaisedButton primary={true} label="CLOSE" onClick={this.handleClose}/>
		];
		return (
			<Dialog title="Authentication Failed" actions={action} open={this.state.show}>
				<div>
					<h4> Your tokens are either missing or expired. Please do the following steps:</h4>
					<ol>
						<li>Authenticate in Jupyterhub</li>
						<li>Close the current open explorers</li>
						<li>Open new explorers from Jupyterhub INCORE app dropdowns</li>
					</ol>
					<h4> Or login clicking <a href="/">HERE</a></h4>
				</div>
			</Dialog>
		);
	}
}

export default Notification;
