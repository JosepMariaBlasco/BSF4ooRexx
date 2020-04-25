package org.oorexx.ide.intellij.util;

import com.google.common.collect.Lists;
import com.intellij.psi.PsiElement;
import com.intellij.psi.tree.IElementType;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;


public class PsiUtil {
    public static List<PsiElement> filter(List<PsiElement> elements, IElementType elementType){
        return elements.stream().filter(new Predicate<PsiElement>() {
            @Override
            public boolean test(PsiElement psiElement) {
                return elementType.equals(psiElement.getNode().getElementType());
            }
        }).collect(Collectors.toList());
    }
    public static List<PsiElement> filter(PsiElement[] elements, IElementType elementType){
        return filter(Lists.newArrayList(elements), elementType);
    }
}
