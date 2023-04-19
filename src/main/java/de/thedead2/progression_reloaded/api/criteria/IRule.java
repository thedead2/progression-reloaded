package de.thedead2.progression_reloaded.api.criteria;

/** Events stuff **/
public interface IRule<T extends IRuleProvider> {
    /** Return the provider **/
    public T getProvider();

    /** Sets the provider for this **/
    public void setProvider(T provider);
}
