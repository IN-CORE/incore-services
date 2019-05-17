import React, { Component } from "react";
import PropTypes from "prop-types";
import { Link, IndexLink } from "react-router";
import { AppBar, FlatButton, MuiThemeProvider } from "material-ui";
import { browserHistory } from "react-router";
import {readCredentials} from "../actions";
import config from "../app.config";

global.__base = `${__dirname  }/`;

class App extends Component {

	constructor(props)  {
		super(props);
		this.state = {
			authError:false,
			authLocationFrom: null,
		};
		this.logout = this.logout.bind(this);
	}

	componentWillMount() {
		let {query} = this.props.location;
		if (Object.keys(query).length > 0) {
			readCredentials(query);
			this.props.router.push(window.location.pathname);
		}
	}

	logout () {
		this.props.logout();
		browserHistory.push(config.baseUrl);
	}

	render() {
		let contents = (<div>
			<FlatButton style={{color: "white"}} label="Home"  containerElement={<IndexLink to={`${config.baseUrl}`}/>} />
		</div>);

		if(this.props.user !== "" && this.props.user !== undefined) {
			contents = (<div>
				<FlatButton style={{color: "white"}} label="Fragility Explorer" containerElement={<Link to={`${config.urlPrefix}/FragilityViewer`} />} />
				<FlatButton style={{color: "white"}} label="Data Explorer" containerElement={<Link to={`${config.urlPrefix}/DataViewer`} />} />
				<FlatButton style={{color: "white"}} label="Hazard Explorer" containerElement={<Link to={`${config.urlPrefix}/HazardViewer`} />} />
				<FlatButton style={{color: "white"}} label="Logout" containerElement={<Link to={`${config.baseUrl}`} />} onClick={this.logout} />
			</div>);
		}

		return (
			<div>
				<MuiThemeProvider>
					<div>
						<AppBar title="IN-CORE v2" showMenuIconButton={false} iconStyleRight={{margin: "auto"}}
								iconElementRight= {contents} />
						{this.props.children}
					</div>
				</MuiThemeProvider>
			</div>
		);
	}

}

App.propTypes = {
	children: PropTypes.element,
	logout: PropTypes.func,
	user: PropTypes.string
};

export default App;
