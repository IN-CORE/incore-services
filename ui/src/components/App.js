import React, { Component } from "react";
import PropTypes from "prop-types";
import { Link, IndexLink } from "react-router";
import { AppBar, FlatButton, MuiThemeProvider } from "material-ui";
import { browserHistory } from "react-router";

global.__base = `${__dirname  }/`;

class App extends Component {

	constructor(props)  {
		super(props);
		this.logout = this.logout.bind(this);
	}

	logout () {
		this.props.logout();
		browserHistory.push("/");
	}

	render() {

		let contents = (<div>
			<FlatButton style={{color: "white"}} label="Home"  containerElement={<IndexLink to="/" />} />
		</div>);


		if(this.props.user !== "" && this.props.user !== undefined) {
			contents = <div>
				{/*<FlatButton style={{color: "white"}} label="Home"  containerElement={<IndexLink to="/" />} />*/}
				<FlatButton style={{color: "white"}} label="Fragility Explorer" containerElement={<Link to="/FragilityViewer" />} />
				<FlatButton style={{color: "white"}} label="Data Explorer" containerElement={<Link to="/DataViewer" />} />
				<FlatButton style={{color: "white"}} label="Hazard Explorer" containerElement={<Link to="/HazardViewer" />} />
				<FlatButton style={{color: "white"}} label="Analysis" containerElement={<Link to="/Execute" />} />
				<FlatButton style={{color: "white"}} label="Help" containerElement={<Link to="/Help" />} />
				<FlatButton style={{color: "white"}} label="Logout" containerElement={<Link to="/"/>} onClick={this.logout} />
			</div>;
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
	children: PropTypes.element
};

export default App;
