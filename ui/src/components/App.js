import React, { Component } from "react";
import PropTypes from "prop-types";
import { Link, IndexLink } from "react-router";
import { AppBar, FlatButton, MuiThemeProvider } from "material-ui";

global.__base = `${__dirname  }/`;

class App extends Component {

	render() {

		let contents = (<div>
			<FlatButton style={{color: "white"}} label="Home"  containerElement={<IndexLink to="/" />} />
		</div>);

		if(this.props.user !== "") {
			contents = <div>
					<FlatButton style={{color: "white"}} label="Home"  containerElement={<IndexLink to="/" />} />
					<FlatButton style={{color: "white"}} label="Fragility Explorer" containerElement={<Link to="/FragilityViewer" />} />
					<FlatButton style={{color: "white"}} label="Data Explorer" containerElement={<Link to="/DataViewer" />} />
					<FlatButton style={{color: "white"}} label="Analysis" containerElement={<Link to="/Execute" />} />
					<FlatButton style={{color: "white"}} label="Help" containerElement={<Link to="/Help" />} />
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
