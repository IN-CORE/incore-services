import React, {Component} from "react";
import {browserHistory} from "react-router";
import {Textfield, Button, Grid, Cell, Display2} from "react-mdc-web";

type Props = {
	name: string
}

class HomePage extends Component {
	constructor(props) {
		super(props);
		this.state = {
			username:"",
			password:""
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

	render () {

		return (
			<div className="center">
				<Grid>
					<Cell col={4} tablet={2} phone={1}/>
					<Cell col={4} tablet={4} phone={3} >
						<Display2>InCore V2 Login</Display2>

						<Textfield
							floatingLabel = "Username"
							value={this.state.username}
							onChange={this.changeUsername}

						/>
						<Textfield
							floatingLabel="Password"
							type="password"
							minLength={8}
							helptext = "Your password must be at least 8 characters"
							helptextValidation
							value={this.state.password}
							onChange={this.changePassword}
						/>

						<Button primary raised onClick={this.login}> Login </Button>
					</Cell>
					<Cell col={4} tablet={2} phone={1}/>
				</Grid>
			</div>
		);

	}
}

export default HomePage;
