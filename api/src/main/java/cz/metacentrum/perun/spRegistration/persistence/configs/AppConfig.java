package cz.metacentrum.perun.spRegistration.persistence.configs;

import cz.metacentrum.perun.spRegistration.persistence.models.PerunAttributeDefinition;
import cz.metacentrum.perun.spRegistration.persistence.rpc.PerunConnector;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

public class AppConfig {

	private String idpAttribute;
	private String idpAttributeValue;
		private String adminsAttr;
	private Set<Long> admins;
	private String extSourceProxy;
	private String userEmailAttr;
	private final Map<String, PerunAttributeDefinition> perunAttributeDefinitionsMap = new HashMap<>();
	private boolean oidcEnabled;
	private List<String> langs = new ArrayList<>();

	private final Properties enLocale = new Properties();
	private final Properties csLocale = new Properties();

	private PerunConnector connector;
	private String showOnServicesListAttribute;
	private String testSpAttribute;
	private String footerHTML;
	private String headerLogo;
	private String headerTitle;
	private String headerHTML;
	private long confirmationPeriodDays;
	private long confirmationPeriodHours;
	private String hashSalt;
	private boolean specifyAuthoritiesEnabled;

	public AppConfig() {
		Resource enLang = new ClassPathResource("localization.properties");
		Resource csLang = new ClassPathResource("localization_cs.properties");
		try (InputStream en = enLang.getInputStream(); InputStream cs = csLang.getInputStream()) {
			enLocale.load(en);
			csLocale.load(cs);
		} catch (IOException e) {
			throw new RuntimeException("Cannot load translations", e);
		}
	}

	public String getIdpAttribute() {
		return idpAttribute;
	}

	public void setIdpAttribute(String idpAttribute) {
		this.idpAttribute = idpAttribute;
	}

	public String getIdpAttributeValue() {
		return idpAttributeValue;
	}

	public void setIdpAttributeValue(String idpAttributeValue) {
		this.idpAttributeValue = idpAttributeValue;
	}

	public Set<Long> getAdmins() {
		return Collections.unmodifiableSet(admins);
	}

	public void setAdmins(Set<Long> admins) {
		this.admins = admins;
	}

	public boolean isOidcEnabled() {
		return oidcEnabled;
	}

	public void setOidcEnabled(boolean oidcEnabled) {
		this.oidcEnabled = oidcEnabled;
	}

	public List<String> getLangs() {
		return langs;
	}

	public void setLangs(List<String> langs) {
		this.langs = langs;
	}

	public PerunConnector getConnector() {
		return connector;
	}

	public void setConnector(PerunConnector connector) {
		this.connector = connector;
	}

	public String getTestSpAttribute() {
		return testSpAttribute;
	}

	public void setTestSpAttribute(String testSpAttribute) {
		this.testSpAttribute = testSpAttribute;
	}

	public String getShowOnServicesListAttribute() {
		return showOnServicesListAttribute;
	}

	public void setShowOnServicesListAttribute(String showOnServicesListAttribute) {
		this.showOnServicesListAttribute = showOnServicesListAttribute;
	}

	public Map<String, PerunAttributeDefinition> getPerunAttributeDefinitionsMap() {
		return perunAttributeDefinitionsMap;
	}

	public String getAdminsAttr() {
		return adminsAttr;
	}

	public void setAdminsAttr(String adminsAttr) {
		this.adminsAttr = adminsAttr;
	}

	public String getExtSourceProxy() {
		return extSourceProxy;
	}

	public void setExtSourceProxy(String extSourceProxy) {
		this.extSourceProxy = extSourceProxy;
	}

	public String getUserEmailAttr() {
		return userEmailAttr;
	}

	public void setUserEmailAttr(String userEmailAttr) {
		this.userEmailAttr = userEmailAttr;
	}

	public String getFooterHTML() {
		return footerHTML;
	}

	public void setFooterHTML(String footerHTML) {
		this.footerHTML = footerHTML;
	}

	public String getHeaderLogo() {
		return headerLogo;
	}

	public void setHeaderLogo(String headerLogo) {
		this.headerLogo = headerLogo;
	}

	public String getHeaderTitle() {
		return headerTitle;
	}

	public void setHeaderTitle(String headerTitle) {
		this.headerTitle = headerTitle;
	}

	public String getHeaderHTML() {
		return headerHTML;
	}

	public void setHeaderHTML(String headerHTML) {
		this.headerHTML = headerHTML;
	}

	public void setConfirmationPeriodDays(long confirmationPeriodDays) {
		this.confirmationPeriodDays = confirmationPeriodDays;
	}

	public long getConfirmationPeriodDays() {
		return confirmationPeriodDays;
	}

	public void setConfirmationPeriodHours(long confirmationPeriodHours) {
		this.confirmationPeriodHours = confirmationPeriodHours;
	}

	public long getConfirmationPeriodHours() {
		return confirmationPeriodHours;
	}

	public void setHashSalt(String hashSalt) {
		this.hashSalt = hashSalt;
	}

	public String getHashSalt() {
		return hashSalt;
	}

	public void setSpecifyAuthoritiesEnabled(boolean specifyAuthoritiesEnabled) {
		this.specifyAuthoritiesEnabled = specifyAuthoritiesEnabled;
	}

	public boolean getSpecifyAuthoritiesEnabled() {
		return specifyAuthoritiesEnabled;
	}

	@Override
	public String toString() {
		return "AppConfig{" +
				"idpAttribute='" + idpAttribute + '\'' +
				", idpAttributeValue='" + idpAttributeValue + '\'' +
				", testSpAttribute='" + testSpAttribute + '\'' +
				", adminsAttr='" + adminsAttr + '\'' +
				", admins=" + admins +
				", userEmailAttr=" + userEmailAttr +
				", extSourceProxy=" + extSourceProxy +
				", perunAttributeDefinitionsMap=" + perunAttributeDefinitionsMap +
				", oidcEnabled=" + oidcEnabled +
				", langs=" + langs +
				", enLocale=" + enLocale +
				", csLocale=" + csLocale +
				", connector=" + connector +
				", footerHTML='" + footerHTML + '\'' +
				", headerLogo='" + headerLogo + '\'' +
				", headerTitle='" + headerTitle + '\'' +
				", headerHTML='" + headerHTML + '\'' +
				", confirmationPeriodDays='" + confirmationPeriodDays + '\'' +
				", confirmationPeriodHours='" + confirmationPeriodHours + '\'' +
				", hashSalt'" + hashSalt + '\'' +
				", specifyAuthoritiesEnabled='" + specifyAuthoritiesEnabled + '\'' +
				'}';
	}

	// custom methods

	public Properties getEnLocale() {
		return enLocale;
	}

	public Properties getCsLocale() {
		return csLocale;
	}

	public PerunAttributeDefinition getAttrDefinition(String fullName) {
		return perunAttributeDefinitionsMap.get(fullName);
	}

	public boolean isAdmin (Long userId) {
		return admins.contains(userId);
	}
}
