import React, { Component } from "react";
import PropTypes from "prop-types";
import { Link, IndexLink } from "react-router";
import { AppBar, FlatButton, MuiThemeProvider, Toolbar, ToolbarGroup, ToolbarTitle } from "material-ui";

global.__base = `${__dirname  }/`;

class App extends Component {

	render() {
		return (
			<div>
				<MuiThemeProvider>
					<div>
						<AppBar title="IN-CORE v2" iconElementRight={
							<div>
								<FlatButton label="Home"  containerElement={<Link to="/" />} />
								<FlatButton label="Fragility Explorer" containerElement={<Link to="/FragilityViewer" />} />
								<FlatButton label="Data Explorer" containerElement={<Link to="/DataViewer" />} />
								<FlatButton label="Analysis" containerElement={<Link to="/Analysis" />} />
								<FlatButton label="Help" containerElement={<Link to="/Help" />} />
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
