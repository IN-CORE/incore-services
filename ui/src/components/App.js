import React, { Component } from "react";
import PropTypes from "prop-types";
import "material-components-web/dist/material-components-web.min.css";

global.__base = `${__dirname  }/`;

class App extends Component {

	render(){
		return (<div>
			{this.props.children}
		</div>);
	}
}

App.propTypes = {
	children: PropTypes.element
};

export default App;
