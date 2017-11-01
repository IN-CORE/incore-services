import React, { Component } from "react";
import Handsontable from "handsontable";
import "handsontable/dist/handsontable.css";

class Table extends Component {

	constructor(props) {
		super(props);
	}

	componentDidMount() {
		this.datagrid = new Handsontable(
			document.getElementById(this.props.container), {
				data: this.props.data,
				rowHeaders: this.props.rowHeaders,
				colHeaders: this.props.colHeaders,
				observeChanges: true
			});
	}

	componentWillUpdate() {
		this.datagrid.destroy();
		this.datagrid = new Handsontable(
			document.getElementById(this.props.container), {
				data: this.props.data,
				rowHeaders: this.props.rowHeaders,
				colHeaders: this.props.colHeaders,
				observeChanges: true
			});
	}

	componentWillUnmount() {
		this.datagrid.destroy();
	}

	render() {
		return <div id={this.props.container} />;

	}
}

export default Table;
