import React, { Component } from "react";
import { browserHistory } from "react-router";
import { TextField, GridTile, GridList, RaisedButton, Card, CardText, CardTitle, CardHeader, Paper } from "material-ui";

type Props = {
	name: string
}

class HomePage extends Component {
	constructor(props) {
		super(props);
		this.state = {
			username: "",
			password: ""
		};
		this.changeUsername = this.changeUsername.bind(this);
		this.changePassword = this.changePassword.bind(this);
		this.login = this.login.bind(this);
	}

	changeUsername(event: Object) {
		this.setState({username: event.target.value});
	}

	changePassword(event: Object) {
		this.setState({password: event.target.value});
	}

	login(event: Object) {
		//TODO: Login using a global state action
		browserHistory.push("/Execute");
	}

	render() {

		return (
			<div className="center" style={{display: "block", margin: "auto", width: "500px", paddingTop: "10%"}}>
				<Paper zDepth={3} style={{padding: 20}}>
					<h2>IN-CORE v2 Login</h2>

					<GridList cellHeight="auto" cols={1}>
						<GridTile>
							<TextField
								floatingLabelText="Username"
								value={this.state.username}
								onChange={this.changeUsername}
							/>
						</GridTile>

						<GridTile>
							<TextField
								floatingLabelText="Password"
								type="password"
								minLength={8}
								hintText="Your password must be at least 8 characters"
								helptextValidation
								value={this.state.password}
								onChange={this.changePassword}
							/>
						</GridTile>

						<GridTile style={{paddingTop: "20px"}}>
							<RaisedButton primary={true} onClick={this.login}> Login </RaisedButton>
						</GridTile>
					</GridList>
				</Paper>
			</div>
		);

	}
}

export default HomePage;
