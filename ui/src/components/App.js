import React, { PropTypes, Component } from "react";
import {IndexLink } from "react-router";
import "material-components-web/dist/material-components-web.min.css";
global.__base = `${__dirname  }/`;

class App extends Component {
	componentWillMount() {
		this.props.loadAnalyses();
	}

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
