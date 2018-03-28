import React, {Component} from "react";
import {MenuItem, SelectField} from "material-ui";

class InputDropdown extends Component {

	constructor(props) {
		super(props);
		this.state = {};
		this.onChange = this.onChange.bind(this);
	}

	onChange(event, index, value) {
		this.props.onChange(this.props.input.name, value);
	}
	render() {
		const filtered = this.props.options.filter(dataset => this.props.input.type.indexOf(dataset.dataType) > -1);
		const menu_options = filtered.map(option =>
			<MenuItem key={option.id} value={option.id} primaryText={option.title}/>
		);
		return (
		<div key={this.props.input.id}>
				<SelectField id={this.props.input.id}
							 key={`select-${this.props.input.id}`}
				             floatingLabelText={this.props.input.name}
				             value={this.props.value}
				             onChange={this.onChange}>
					{menu_options}
				</SelectField>
			<br/>
		</div>

		);
	}

}

export default InputDropdown;
