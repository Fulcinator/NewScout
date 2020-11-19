package plugin;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class SuggestWordsTest
{
	@Test
	public void test()
	{
		SuggestWords suggestWords=new SuggestWords();
		List<Category> categories1=suggestWords.getCategories("huston");
		List<Category> categories2=suggestWords.getCategories("chicago");
		List<List<Category>> categorysList=new ArrayList<List<Category>>();
		categorysList.add(categories1);
		categorysList.add(categories2);
		Category mostCommonCategory=suggestWords.mostCommonCategory(categorysList);
		Assert.assertTrue(mostCommonCategory.getWords().size()>0);
	}
}
