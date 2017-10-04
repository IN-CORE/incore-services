import React, {PureComponent} from "react";
import PropTypes from "prop-types";

class Select extends PureComponent {

	static propTypes = {
		selectedIndex: PropTypes.number,
		children: PropTypes.arrayOf(Object),
		onChange: PropTypes.func,
		label: PropTypes.string,
		value: PropTypes.string,
		disabled: PropTypes.bool,
		dataIdx: PropTypes.number

	};

	constructor(props: Object) {
		super(props);
	}

	render() {

		const classNames = "mdc-select " + this.props.className;
		return(

			<select value={this.props.value} className={classNames} onChange={this.props.onChange.bind(this)} data-idx={this.props.dataIdx} disabled={this.props.disabled} >
				{this.props.children}
			</select>
		)
	}
}

export default Select;
