package org.dominokit.domino.ui.forms;

import elemental2.dom.HTMLInputElement;
import org.dominokit.domino.ui.forms.validations.InputAutoValidator;
import org.dominokit.domino.ui.forms.validations.ValidationResult;
import org.gwtproject.i18n.client.NumberFormat;
import org.jboss.elemento.Elements;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public abstract class NumberBox<T extends NumberBox<T, E>, E extends Number> extends AbstractValueBox<T, HTMLInputElement, E> {

    private final ChangeHandler<E> formatValueChangeHandler = value -> formatValue();
    private String maxValueErrorMessage;
    private String minValueErrorMessage;
    private String invalidFormatMessage;
    private boolean formattingEnabled;
    private String pattern = null;

    public NumberBox(String label) {
        super("tel", label);
        addInputStringValidator();
        addMaxValueValidator();
        addMinValueValidator();
        setAutoValidation(true);
        enableFormatting();
    }

    private void addInputStringValidator() {
        addValidator(() -> {
            String inputValue = getInputElement().element().value;
            if(nonNull(inputValue) && !inputValue.isEmpty()) {
                try {
                    getNumberFormat().parse(inputValue);
                } catch (NumberFormatException e) {
                    return ValidationResult.invalid(getInvalidFormatMessage());
                }
            }
            return ValidationResult.valid();
        });
    }

    private void addMaxValueValidator() {
        addValidator(() -> {
            E value = getValue();
            if (nonNull(getMaxValue())) {
                String inputValue = getInputElement().element().value;
                if (nonNull(inputValue) && !inputValue.isEmpty()) {
                    try {
                        double parsed = getNumberFormat().parse(inputValue);
                        if (nonNull(value) && isExceedMaxValue(getMaxDoubleValue(), parsed)) {
                            return ValidationResult.invalid(getMaxValueErrorMessage());
                        }
                    } catch (NumberFormatException e) {
                        return ValidationResult.invalid(getInvalidFormatMessage());
                    }
                }
            }
            return ValidationResult.valid();
        });
    }

    protected Double getMaxDoubleValue() {
        if (nonNull(getMaxValue())) {
            return getMaxValue().doubleValue();
        }
        return null;
    }

    private void addMinValueValidator() {
        addValidator(() -> {
            E value = getValue();
            if (nonNull(value) && isLowerThanMinValue(value)) {
                return ValidationResult.invalid(getMinValueErrorMessage());
            }
            return ValidationResult.valid();
        });
    }

    private void formatValue() {
        String stringValue = getStringValue();
        if (nonNull(stringValue) && !stringValue.isEmpty()) {
            try {
                double parsedValue = getNumberFormat().parse(stringValue);
                getInputElement().element().value = getNumberFormat().format(parsedValue);
            } catch (NumberFormatException e) {
                // nothing to format, so we do nothing!
            }
        }
    }

    @Override
    protected HTMLInputElement createInputElement(String type) {
        return Elements.input(type).element();
    }

    @Override
    protected void doSetValue(E value) {
        if (nonNull(value)) {
            getInputElement().element().value = getNumberFormat().format(value);
        } else {
            getInputElement().element().value = "";
        }
    }

    @Override
    public E getValue() {
        String value = getInputElement().element().value;
        try {
            if (value.isEmpty()) {
                return null;
            }
            E parsedValue = parseValue(value);
            return parsedValue;
        } catch (NumberFormatException e) {
            invalidate(value.startsWith("-") ? getMinValueErrorMessage() : getMaxValueErrorMessage());
            return null;
        }
    }

    @Override
    public boolean isEmpty() {
        return getInputElement().element().value.isEmpty();
    }

    @Override
    public String getStringValue() {
        return getInputElement().element().value;
    }

    public T setMinValue(E minValue) {
        getInputElement().element().min = minValue + "";
        return (T) this;
    }

    public T setMaxValue(E maxValue) {
        getInputElement().element().max = maxValue + "";
        return (T) this;
    }

    public T setStep(E step) {
        getInputElement().element().step = step + "";
        return (T) this;
    }

    public E getMaxValue() {
        String maxValue = getInputElement().element().max;
        if (isNull(maxValue) || maxValue.isEmpty()) {
            return defaultMaxValue();
        }
        return parseValue(maxValue);
    }

    public E getMinValue() {
        String minValue = getInputElement().element().min;
        if (isNull(minValue) || minValue.isEmpty()) {
            return defaultMinValue();
        }
        return parseValue(minValue);
    }

    public E getStep() {
        String step = getInputElement().element().step;
        if (isNull(step) || step.isEmpty()) {
            return null;
        }
        return parseValue(step);
    }

    public T setMaxValueErrorMessage(String maxValueErrorMessage) {
        this.maxValueErrorMessage = maxValueErrorMessage;
        return (T) this;
    }

    public T setMinValueErrorMessage(String minValueErrorMessage) {
        this.minValueErrorMessage = minValueErrorMessage;
        return (T) this;
    }

    public T setInvalidFormatMessage(String invalidFormatMessage) {
        this.invalidFormatMessage = invalidFormatMessage;
        return (T) this;
    }

    public String getMaxValueErrorMessage() {
        return isNull(maxValueErrorMessage) ? "Maximum allowed value is [" + getMaxValue() + "]" : maxValueErrorMessage;
    }

    public String getMinValueErrorMessage() {
        return isNull(minValueErrorMessage) ? "Minimum allowed value is [" + getMinValue() + "]" : minValueErrorMessage;
    }

    public String getInvalidFormatMessage() {
        return isNull(invalidFormatMessage) ? "Invalid number format" : invalidFormatMessage;
    }

    private boolean isExceedMaxValue(E value) {
        E maxValue = getMaxValue();
        if (isNull(maxValue))
            return false;
        return isExceedMaxValue(maxValue, value);
    }

    private boolean isExceedMaxValue(double maxAsDouble, double valueAsDouble) {
        return valueAsDouble > maxAsDouble;
    }

    private boolean isLowerThanMinValue(E value) {
        E minValue = getMinValue();
        if (isNull(minValue))
            return false;
        return isLowerThanMinValue(minValue, value);
    }

    public T enableFormatting() {
        return setFormattingEnabled(true);
    }

    public T disableFormatting() {
        return setFormattingEnabled(false);
    }

    private T setFormattingEnabled(boolean formattingEnabled) {
        this.formattingEnabled = formattingEnabled;
        if (formattingEnabled) {
            if (!hasChangeHandler(formatValueChangeHandler)) {
                addChangeHandler(formatValueChangeHandler);
            }
        } else {
            removeChangeHandler(formatValueChangeHandler);
        }
        return (T) this;
    }

    protected NumberFormat getNumberFormat() {
        if (nonNull(getPattern())) {
            return NumberFormat.getFormat(getPattern());
        } else {
            return NumberFormat.getDecimalFormat();
        }
    }

    public String getPattern() {
        return pattern;
    }

    public T setPattern(String pattern) {
        this.pattern = pattern;
        return (T) this;
    }

    protected abstract E parseValue(String value);

    protected abstract E defaultMaxValue();

    protected abstract E defaultMinValue();

    protected abstract boolean isExceedMaxValue(E maxValue, E value);

    protected abstract boolean isLowerThanMinValue(E minValue, E value);

    @Override
    protected AutoValidator createAutoValidator(AutoValidate autoValidate) {
        return new InputAutoValidator<>(getInputElement(), autoValidate);
    }
}
