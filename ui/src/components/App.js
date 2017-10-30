import React, { Component } from "react";
import PropTypes from "prop-types";
import { Link, IndexLink } from "react-router";
import { AppBar, FlatButton, MuiThemeProvider } from "material-ui";

global.__base = `${__dirname  }/`;

class App extends Component {

	render() {
		return (
			<div>
				<MuiThemeProvider>
					<div>
						<AppBar title="IN-CORE v2" showMenuIconButton={false} iconStyleRight={{margin: "auto"}}
								iconElementRight= {
							<div>
								<FlatButton style={{color: "white"}} label="Home"  containerElement={<IndexLink to="/" />} />
								<FlatButton style={{color: "white"}} label="Fragility Explorer" containerElement={<Link to="/FragilityViewer" />} />
								<FlatButton style={{color: "white"}} label="Data Explorer" containerElement={<Link to="/DataViewer" />} />
								<FlatButton style={{color: "white"}} label="Analysis" containerElement={<Link to="/Analysis" />} />
								<FlatButton style={{color: "white"}} label="Help" containerElement={<Link to="/Help" />} />
							</div>
						} />
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
