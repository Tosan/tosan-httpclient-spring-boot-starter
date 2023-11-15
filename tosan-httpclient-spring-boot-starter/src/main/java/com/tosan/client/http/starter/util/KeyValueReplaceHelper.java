package com.tosan.client.http.starter.util;

import com.tosan.tools.mask.starter.business.ComparisonTypeFactory;
import com.tosan.tools.mask.starter.business.ValueMaskFactory;
import com.tosan.tools.mask.starter.business.enumeration.MaskType;
import com.tosan.tools.mask.starter.config.SecureParameter;
import com.tosan.tools.mask.starter.replace.ReplaceHelper;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Ali Alimohammadi
 * @since 11/14/2023
 */
public class KeyValueReplaceHelper extends ReplaceHelper {

    public KeyValueReplaceHelper(ValueMaskFactory valueMaskFactory, ComparisonTypeFactory comparisonTypeFactory) {
        super(valueMaskFactory, comparisonTypeFactory);
    }

    @Override
    public String replace(String input, Map<String, SecureParameter> securedParameterNames) {
        Pattern pattern = Pattern.compile("([^\\s;]+)([ ]*[=][ ]*)([^;]+)");
        Matcher matcher = pattern.matcher(input);

        while (matcher.find()) {
            String tag = matcher.group(1);
            MaskType maskType = this.checkAndGetMaskType(tag, securedParameterNames);
            if (maskType != null) {
                String originalTag = tag + matcher.group(2) + matcher.group(3);
                String value = matcher.group(3);
                String toBeReplacedTag;
                if (value == null) {
                    toBeReplacedTag = tag + matcher.group(2) + "null";
                } else {
                    String maskedValue = this.maskValue(value, maskType);
                    toBeReplacedTag = tag + matcher.group(2) + maskedValue;
                }

                input = input.replace(originalTag, toBeReplacedTag);
            }
        }

        return input;
    }
}
